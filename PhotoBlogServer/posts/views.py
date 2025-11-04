from django.shortcuts import render, redirect
from .models import Post
from .serializers import PostSerializer
from rest_framework import viewsets
from rest_framework.response import Response
from rest_framework.decorators import api_view
from .forms import PostForm

# -------------------------------
# DRF API ViewSet
# -------------------------------
class PostViewSet(viewsets.ModelViewSet):
    queryset = Post.objects.all().order_by('-created_at')
    serializer_class = PostSerializer

    # Ensure absolute image URLs for Android
    def get_serializer_context(self):
        context = super().get_serializer_context()
        context.update({"request": self.request})
        return context

# -------------------------------
# Optional: simple API view for list
# -------------------------------
@api_view(['GET'])
def post_list_api(request):
    posts = Post.objects.all().order_by('-created_at')
    serializer = PostSerializer(posts, many=True, context={'request': request})
    return Response(serializer.data)

# -------------------------------
# HTML page view
# -------------------------------
def post_list(request):
    posts = Post.objects.all().order_by('-created_at')
    return render(request, 'posts/index.html', {'posts': posts})

# -------------------------------
# Optional: simple add form (frontend)
# -------------------------------
def add_post(request):
    if request.method == 'POST':
        form = PostForm(request.POST, request.FILES)
        if form.is_valid():
            form.save()
            return redirect('post_list')
    else:
        form = PostForm()
    return render(request, 'posts/add_post.html', {'form': form})
