import rest_framework

from django.urls import path, include, re_path

app_name = 'test_app'
urlpatterns = [
  path('', include('rest_framework.urls', namespace='rest_framework_category'))
]