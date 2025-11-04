# photoblog/urls.py  ← replace "photoblog" with your project folder name
from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static
from rest_framework import routers
from posts.views import PostViewSet, post_list_api

# DRF router for ViewSet
router = routers.DefaultRouter()
router.register(r'api/posts', PostViewSet, basename='post')

urlpatterns = [
    path('admin/', admin.site.urls),
    path('', include('posts.urls')),  # HTML views
    path('api/', include(router.urls)),  # DRF ViewSet URLs
    path('api/posts-list/', post_list_api),  # Optional simple API for Android
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
