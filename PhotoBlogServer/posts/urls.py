# posts/urls.py
from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import PostViewSet, post_list, add_post, post_list_api

# DRF Router
router = DefaultRouter()
router.register(r'posts', PostViewSet)

urlpatterns = [
    # HTML Views
    path('', post_list, name='post_list'),
    path('add/', add_post, name='add_post'),
 
    # API Endpoints
    path('api/', include(router.urls)),      
    path('api/posts-list/', post_list_api),  
]
