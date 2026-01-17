/*
Copyright 2026.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package controller

import (
	"context"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"

	"github.com/go-logr/logr"
	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	apierrors "k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/util/intstr"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	logf "sigs.k8s.io/controller-runtime/pkg/log"

	stabledwkv1 "stable.dwk/api/v1"
)

type DummySiteReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

// +kubebuilder:rbac:groups=stable.dwk.stable.dwk,resources=dummysites,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=stable.dwk.stable.dwk,resources=dummysites/status,verbs=get;update;patch
// +kubebuilder:rbac:groups=stable.dwk.stable.dwk,resources=dummysites/finalizers,verbs=update
// +kubebuilder:rbac:groups=apps,resources=deployments,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups="",resources=services,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups="",resources=configmaps,verbs=get;list;watch;create;update;patch;delete

const (
	statusConditionTypeAvailable   = "Available"
	statusConditionTypeProgressing = "Progressing"
	statusConditionTypeDegraded    = "Degraded"
	maxConfigMapDataSize           = 900 * 1024
	maxRedirects                   = 1
)

func (r *DummySiteReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	log := logf.FromContext(ctx)

	dummySite := &stabledwkv1.DummySite{}
	if err := r.Get(ctx, req.NamespacedName, dummySite); err != nil {
		if apierrors.IsNotFound(err) {
			log.Info("DummySite resource not found. Ignoring since object must be deleted")
			return ctrl.Result{}, nil
		}
		log.Error(err, "Failed to get DummySite")
		return ctrl.Result{}, err
	}

	if dummySite.Spec.WebsiteURL == "" {
		log.Info("WebsiteURL is not specified in DummySite spec")
		meta.SetStatusCondition(&dummySite.Status.Conditions, metav1.Condition{
			Type:               statusConditionTypeDegraded,
			Status:             metav1.ConditionTrue,
			ObservedGeneration: dummySite.Generation,
			Reason:             "MissingWebsiteURL",
			Message:            "WebsiteURL must be specified in spec",
		})
		if err := r.Status().Update(ctx, dummySite); err != nil {
			log.Error(err, "Failed to update DummySite status")
		}
		return ctrl.Result{}, nil
	}

	configMap := &corev1.ConfigMap{
		ObjectMeta: metav1.ObjectMeta{
			Name:      dummySite.Name + "-content",
			Namespace: dummySite.Namespace,
		},
	}

	if _, err := ctrl.CreateOrUpdate(ctx, r.Client, configMap, func() error {
		configMap.Data = make(map[string]string)

		websiteContent, err := downloadWebsite(ctx, log, dummySite.Spec.WebsiteURL)
		if err != nil {
			log.Error(err, "Failed to download website", "url", dummySite.Spec.WebsiteURL)
			return err
		}

		if len(websiteContent) > maxConfigMapDataSize {
			err := fmt.Errorf("downloaded website content is too large: %d bytes (max %d bytes)", len(websiteContent), maxConfigMapDataSize)
			log.Error(err, "Website content exceeds ConfigMap size limit", "url", dummySite.Spec.WebsiteURL)
			return err
		}

		configMap.Data["index.html"] = websiteContent
		return ctrl.SetControllerReference(dummySite, configMap, r.Scheme)
	}); err != nil {
		log.Error(err, "Failed to create or update ConfigMap")
		if err := r.Get(ctx, req.NamespacedName, dummySite); err != nil {
			log.Error(err, "Failed to get DummySite for status update")
			return ctrl.Result{}, err
		}
		meta.SetStatusCondition(&dummySite.Status.Conditions, metav1.Condition{
			Type:               statusConditionTypeDegraded,
			Status:             metav1.ConditionTrue,
			ObservedGeneration: dummySite.Generation,
			Reason:             "DownloadFailed",
			Message:            fmt.Sprintf("Failed to download or process website: %v", err),
		})
		if statusErr := r.Status().Update(ctx, dummySite); statusErr != nil {
			log.Error(statusErr, "Failed to update DummySite status")
		}
		return ctrl.Result{}, nil
	}

	deployment := &appsv1.Deployment{
		ObjectMeta: metav1.ObjectMeta{
			Name:      dummySite.Name,
			Namespace: dummySite.Namespace,
		},
	}

	if _, err := ctrl.CreateOrUpdate(ctx, r.Client, deployment, func() error {
		replicas := int32(1)
		deployment.Spec.Replicas = &replicas
		deployment.Spec.Selector = &metav1.LabelSelector{
			MatchLabels: map[string]string{
				"app": dummySite.Name,
			},
		}

		deployment.Spec.Template = corev1.PodTemplateSpec{
			ObjectMeta: metav1.ObjectMeta{
				Labels: map[string]string{
					"app": dummySite.Name,
				},
			},
			Spec: corev1.PodSpec{
				Containers: []corev1.Container{
					{
						Name:  "http-server",
						Image: "python:3.11-alpine",
						Command: []string{
							"sh",
							"-c",
							"cd /app && python3 -m http.server 8080",
						},
						Ports: []corev1.ContainerPort{
							{
								ContainerPort: 8080,
							},
						},
						VolumeMounts: []corev1.VolumeMount{
							{
								Name:      "website-content",
								MountPath: "/app",
							},
						},
					},
				},
				Volumes: []corev1.Volume{
					{
						Name: "website-content",
						VolumeSource: corev1.VolumeSource{
							ConfigMap: &corev1.ConfigMapVolumeSource{
								LocalObjectReference: corev1.LocalObjectReference{
									Name: configMap.Name,
								},
								DefaultMode: func(p int32) *int32 { return &p }(0644),
							},
						},
					},
				},
			},
		}

		return ctrl.SetControllerReference(dummySite, deployment, r.Scheme)
	}); err != nil {
		log.Error(err, "Failed to create or update Deployment")
		return ctrl.Result{}, err
	}

	service := &corev1.Service{
		ObjectMeta: metav1.ObjectMeta{
			Name:      dummySite.Name,
			Namespace: dummySite.Namespace,
		},
	}

	if _, err := ctrl.CreateOrUpdate(ctx, r.Client, service, func() error {
		service.Spec.Type = corev1.ServiceTypeClusterIP
		service.Spec.Selector = map[string]string{
			"app": dummySite.Name,
		}
		service.Spec.Ports = []corev1.ServicePort{
			{
				Port:       80,
				TargetPort: intstr.FromInt(8080),
				Protocol:   corev1.ProtocolTCP,
			},
		}

		return ctrl.SetControllerReference(dummySite, service, r.Scheme)
	}); err != nil {
		log.Error(err, "Failed to create or update Service")
		return ctrl.Result{}, err
	}

	if err := r.Get(ctx, req.NamespacedName, dummySite); err != nil {
		log.Error(err, "Failed to get DummySite for final status update")
		return ctrl.Result{}, err
	}

	meta.SetStatusCondition(&dummySite.Status.Conditions, metav1.Condition{
		Type:               statusConditionTypeAvailable,
		Status:             metav1.ConditionTrue,
		ObservedGeneration: dummySite.Generation,
		Reason:             "Ready",
		Message:            fmt.Sprintf("Deployment and Service created. Access at http://%s/", dummySite.Name),
	})
	meta.SetStatusCondition(&dummySite.Status.Conditions, metav1.Condition{
		Type:               statusConditionTypeProgressing,
		Status:             metav1.ConditionFalse,
		ObservedGeneration: dummySite.Generation,
		Reason:             "Done",
		Message:            "Deployment and Service are ready",
	})

	if err := r.Status().Update(ctx, dummySite); err != nil {
		log.Error(err, "Failed to update DummySite status")
	}

	return ctrl.Result{}, nil
}

func downloadWebsite(ctx context.Context, logger logr.Logger, url string) (string, error) {
	tmpDir, err := os.MkdirTemp("", "website-")
	if err != nil {
		return "", fmt.Errorf("failed to create temp directory: %w", err)
	}
	defer os.RemoveAll(tmpDir)

	var cmd *exec.Cmd
	if _, err := exec.LookPath("curl"); err == nil {
		htmlFile := filepath.Join(tmpDir, "index.html")
		cmd = exec.CommandContext(ctx, "curl", "-s", "--max-redirs", fmt.Sprintf("%d", maxRedirects), "-m", "30", "-o", htmlFile, url)
	} else if _, err := exec.LookPath("wget"); err == nil {
		cmd = exec.CommandContext(ctx, "wget", "-q", "--timeout=30", "-O", filepath.Join(tmpDir, "index.html"), url)
	} else {
		return "", fmt.Errorf("neither wget nor curl found in PATH")
	}

	if err := cmd.Run(); err != nil {
		return "", fmt.Errorf("failed to download website: %w", err)
	}

	htmlPath := filepath.Join(tmpDir, "index.html")

	if _, err := os.Stat(htmlPath); err != nil {
		return "", fmt.Errorf("downloaded file not found: %w", err)
	}

	fileInfo, err := os.Stat(htmlPath)
	if err != nil {
		return "", fmt.Errorf("failed to stat file: %w", err)
	}

	if fileInfo.Size() > int64(maxConfigMapDataSize) {
		return "", fmt.Errorf("downloaded file is too large: %d bytes (max %d bytes)", fileInfo.Size(), maxConfigMapDataSize)
	}

	content, err := os.ReadFile(htmlPath)
	if err != nil {
		return "", fmt.Errorf("failed to read downloaded file: %w", err)
	}

	logger.Info("Successfully downloaded website", "url", url, "size", len(content))
	return string(content), nil
}

func (r *DummySiteReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&stabledwkv1.DummySite{}).
		Owns(&appsv1.Deployment{}).
		Owns(&corev1.Service{}).
		Owns(&corev1.ConfigMap{}).
		Named("dummysite").
		Complete(r)
}
