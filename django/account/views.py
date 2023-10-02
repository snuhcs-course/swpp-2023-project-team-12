from django.shortcuts import render
from .models import CustomUser
from .serializers import UserCreateSerializer
from rest_framework.response import Response
from rest_framework.views import APIView

# Create your views here.
class SignupView(APIView):
    serializer_class = UserCreateSerializer

    def post(self, request, *args, **kwargs):
        serializer = self.serializer_class(data=request.data)
        if serializer.is_valid():
            user = serializer.save()
            return Response({ 'name': user.username }, status=201)
        return Response(serializer.errors, status=400)
    
