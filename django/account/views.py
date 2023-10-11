from django.shortcuts import render
from .models import CustomUser
from .serializers import UserCreateSerializer, LoginSerializer
from rest_framework.response import Response
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer
from rest_framework.views import APIView

# Create your views here.   
class SignupView(APIView):
    serializer_class = UserCreateSerializer

    def post(self, request):
        serializer = self.serializer_class(data=request.data)
        if serializer.is_valid():
            user = serializer.save()
            return Response({ 'name': user.username }, status=201)
        return Response( {"message": "Signup Failed"}, status=400)
    
class LoginView(APIView):
    serializer_class = LoginSerializer

    def post(self, request):
        serializer = self.serializer_class(data=request.data)
        serializer.is_valid(raise_exception=True)
        user = serializer.validated_data['user']
        token = TokenObtainPairSerializer.get_token(user) 
        refresh_token = str(token)
        access_token = str(token.access_token) 
        response = Response(
            {
                "message": "Login Success",
                "jwt_token": {
                    "access_token": access_token,
                    "refresh_token": refresh_token
                },
            },
            status=200
        )

        return response

