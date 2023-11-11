import random
import secrets
import string
from venv import logger
from django.conf import settings
from account.models import CustomUser
from .serializers import (
    EmailSerializer,
    ResetPasswordSerializer,
    UserCreateSerializer,
    LoginSerializer,
)
from rest_framework.response import Response
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer
from rest_framework.views import APIView
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from django.core.mail import send_mail
from django.db import transaction
from rest_framework.parsers import MultiPartParser, FormParser
from .serializers import UserProfileImageSerializer
from django.core.files.storage import default_storage


# Create your views here.
class SignupView(APIView):
    serializer_class = UserCreateSerializer

    def post(self, request):
        serializer = self.serializer_class(data=request.data)
        if serializer.is_valid():
            user = serializer.save()
            return Response({"name": user.username}, status=201)
        return Response({"message": "Signup Failed"}, status=400)


class LoginView(APIView):
    serializer_class = LoginSerializer

    def post(self, request):
        serializer = self.serializer_class(data=request.data)
        serializer.is_valid(raise_exception=True)
        user = serializer.validated_data["user"]
        token = TokenObtainPairSerializer.get_token(user)
        refresh_token = str(token)
        access_token = str(token.access_token)
        if user.profile_image and user.profile_image.name:
            profile_image_url = (
                settings.MEDIA_URL + "profile_images/" + user.profile_image.name
            )
        else:
            profile_image_url = settings.MEDIA_URL + "temp_profile.jpeg"
        response = Response(
            {
                "message": "Login Success",
                "user": {
                    "user_id": user.id,
                    "username": user.username,
                    "nickname": user.nickname,
                    "email": user.email,
                    "phone_num": user.phone_num,
                    "gender": user.gender,
                    "height": user.height,
                    "weight": user.weight,
                    "age": user.age,
                    "profile_image": profile_image_url if user.profile_image else None,
                },
                "jwt_token": {
                    "access_token": access_token,
                    "refresh_token": refresh_token,
                },
            },
            status=200,
        )

        return response


@api_view(["GET"])
@permission_classes([IsAuthenticated])
def get_user_info(request):
    user = request.user
    return Response({"user_id": user.username})


class FindUsernameAndSendEmailView(APIView):
    serializer_class = EmailSerializer

    def post(self, request):
        serializer = self.serializer_class(data=request.data)
        serializer.is_valid(raise_exception=True)
        email = serializer.validated_data["email"]

        try:
            user = CustomUser.objects.filter(email=email).last()
            subject = "[RunUs] 아이디 찾기"
            message = f"아이디: {user.username}"
            email_from = settings.EMAIL_HOST_USER
            recipient_list = [
                user.email,
            ]
            send_mail(subject, message, email_from, recipient_list)

            return Response({"message": "Email sent successfully"})
        except CustomUser.DoesNotExist:
            return Response({"message": "User not found"}, status=404)


# 임시 비밀번호 생성기
def generate_temp_password(length=10):
    characters = string.ascii_letters + string.digits + string.punctuation
    return "".join(secrets.choice(characters) for i in range(length))


class ResetPasswordView(APIView):
    def post(self, request):
        serializer = ResetPasswordSerializer(data=request.data)
        if serializer.is_valid():
            username = serializer.validated_data["username"]
            email = serializer.validated_data["email"]

            try:
                user = CustomUser.objects.get(username=username, email=email)
                temp_password = generate_temp_password()
                user.set_password(temp_password)
                user.save()

                send_mail(
                    "[RunUs]임시 비밀번호",
                    f"임시 비밀번호: {temp_password}",
                    "from@example.com",
                    [email],
                    fail_silently=False,
                )
                return Response({"message": "Temporary password sent to your email."})
            except CustomUser.DoesNotExist:
                return Response({"error": "No matching user found."}, status=404)
        else:
            return Response(serializer.errors, status=400)


class ProfileImageView(APIView):
    parser_classes = [MultiPartParser, FormParser]
    permission_classes = [IsAuthenticated]

    @transaction.atomic
    def post(self, request, format=None):
        logger.debug(f"Received data: {request.data}")
        user = request.user
        serializer = UserProfileImageSerializer(user, data=request.data)

        if serializer.is_valid():
            serializer.save()
            random_number = random.randint(1, 100000)
            updated_image_url = f"{request.build_absolute_uri(user.profile_image.url)}?v={random_number}"
            logger.debug(f"updated_image_url: {updated_image_url}")
            return Response(
                {
                    "message": "Profile Image Updated Successfully",
                    "imageUrl": updated_image_url,
                },
                status=200,
            )
        else:
            logger.error(f"Serializer errors: {serializer.errors}")
            return Response(serializer.errors, status=400)
