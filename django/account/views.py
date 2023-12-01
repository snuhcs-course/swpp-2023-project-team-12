import random
import secrets
import string
from datetime import timedelta
from venv import logger
from django.conf import settings
from django.shortcuts import get_object_or_404
from account.models import CustomUser
from history.models import HistoryRecord, GroupHistoryRecord
from .serializers import (
    EmailSerializer,
    ResetPasswordSerializer,
    UserCreateSerializer,
    LoginSerializer,
    UserProfileSerializer,
    UsernameEmailSerializer
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


# Create your views here.
class SignupView(APIView):
    authentication_classes = []
    permission_classes = []
    serializer_class = UserCreateSerializer

    def post(self, request):
        serializer = self.serializer_class(data=request.data)
        print(serializer)
        if serializer.is_valid():
            user = serializer.save()
            return Response({"name": user.username}, status=201)
        return Response({"message": "Signup Failed"}, status=400)


class LoginView(APIView):
    authentication_classes = []
    permission_classes = []
    serializer_class = LoginSerializer

    def post(self, request):
        serializer = self.serializer_class(data=request.data)
        serializer.is_valid(raise_exception=True)
        user = serializer.validated_data["user"]
        token = TokenObtainPairSerializer.get_token(user)
        refresh_token = str(token)
        access_token = str(token.access_token)
        print("user.profile_image is ", user.profile_image)
        if user.profile_image and user.profile_image.name:
            profile_image_url = settings.MEDIA_URL + user.profile_image.name
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
                    "exp": user.exp,
                    "profile_image": profile_image_url if user.profile_image else None,
                    "badge_collection": user.badge_collection,
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
            # send_mail(subject, message, email_from, recipient_list)

            return Response({"message": "Email sent successfully"})
        except CustomUser.DoesNotExist:
            return Response({"message": "User not found"}, status=404)


# 임시 비밀번호 생성기
def generate_temp_password(length=10):
    characters = string.ascii_letters + string.digits + string.punctuation
    return "".join(secrets.choice(characters) for i in range(length))


class ResetPasswordView(APIView):
    def post(self, request):
        serializer = UsernameEmailSerializer(data=request.data)
        print(serializer)
        if serializer.is_valid():
            print("hi")
            username = serializer.validated_data["username"]
            email = serializer.validated_data["email"]
            try:
                user = CustomUser.objects.get(username=username, email=email)
                temp_password = generate_temp_password()
                # send_mail(
                #     "[RunUs]인증번호",
                #     f"인증번호: {temp_password}",
                #     "from@example.com",
                #     [email],
                #     fail_silently=False,
                # )
                return Response({"message": temp_password}, status=200)
            except CustomUser.DoesNotExist:
                return Response({"error": "No matching user found."}, status=404)
        else:
            return Response(serializer.errors, status=400)
        
    def patch(self, request):
        serializer = ResetPasswordSerializer(data=request.data)
        print(serializer)
        if serializer.is_valid():
            username = serializer.validated_data["username"]
            email = serializer.validated_data["email"]
            new_password = serializer.validated_data["password"]
            try:
                user = CustomUser.objects.get(username=username, email=email)
                user.set_password(new_password)
                user.save()
                return Response({"message": "Success."}, status=200)
            except CustomUser.DoesNotExist:
                return Response({"error": "No matching user found."}, status=404)
        else:
            print(serializer.errors)
            return Response(serializer.errors, status=400) 

    # def post(self, request):
    #     serializer = UsernameEmailSerializer(data=request.data)
    #     if serializer.is_valid():
    #         username = serializer.validated_data["username"]
    #         email = serializer.validated_data["email"]

    #         try:
    #             user = CustomUser.objects.get(username=username, email=email)
    #             temp_password = generate_temp_password()
    #             user.set_password(temp_password)
    #             user.save()

    #             send_mail(
    #                 "[RunUs]임시 비밀번호",
    #                 f"임시 비밀번호: {temp_password}",
    #                 "from@example.com",
    #                 [email],
    #                 fail_silently=False,
    #             )
    #             return Response({"message": "Temporary password sent to your email."})
    #         except CustomUser.DoesNotExist:
    #             return Response({"error": "No matching user found."}, status=404)
    #     else:
    #         return Response(serializer.errors, status=400)


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
            ##logger.debug(f"updated_image_url: {updated_image_url}")
            print(user.badge_collection)
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


class UserProfileView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request, user_id=None):
        user = get_object_or_404(CustomUser, id=user_id) if user_id else request.user
        print(user)
        ##logger.debug(f"send profile url: {user.profile_image.url}")
        serializer = UserProfileSerializer(user, context={"request": request})
        print(serializer.data)

        return Response(serializer.data)


def processBadgeCollection(badge_collection, history_instance):
    # return 1000000000 #for reset(testing)
    if badge_collection % 10 != 1:
        print("badge1")
        history_instances = HistoryRecord.objects.filter(
            user_id=history_instance.user_id
        )
        print("history count : ", len(history_instances))
        if len(history_instances) >= 5:
            badge_collection += 1
    if ((int)(badge_collection / 10)) % 10 != 1:
        print("badge2")
        if history_instance.is_group is True:
            group_history_list = GroupHistoryRecord.objects.filter(
                id=history_instance.group_history_id
            )
            print(group_history_list)
            group_history_instance = None
            if len(group_history_list) >= 1:
                group_history_instance = group_history_list[0]
                if (
                    group_history_instance.first_place_user_id
                    == history_instance.user_id
                ):
                    badge_collection += 10

    if ((int)(badge_collection / 100)) % 10 != 1:
        print("badge3")
        history_instances = HistoryRecord.objects.filter(
            user_id=history_instance.user_id
        )
        count = 0
        for history in history_instances:
            if history.is_mission_succeeded > 0:
                count += 1

        if count >= 5:
            badge_collection += 100

    if ((int)(badge_collection / 1000)) % 10 != 1:
        print("badge4")
        history_instances = HistoryRecord.objects.filter(
            user_id=history_instance.user_id
        )
        count = 0
        current_date = history_instance.start_time.date()
        print(current_date)
        for i in range(4):
            for history in history_instances:
                if history.start_time.date() == current_date - timedelta(days=1):
                    count += 1
                    current_date = current_date - timedelta(days=1)
                    print(current_date)
                    break
        if count >= 4:
            badge_collection += 1000
    if ((int)(badge_collection / 10000)) % 10 != 1:
        print("badge5")
        if history_instance.is_group is True:
            group_history_list = GroupHistoryRecord.objects.filter(
                first_place_user_id=history_instance.user_id
            )
            if len(group_history_list) >= 10:
                badge_collection += 10000
    if ((int)(badge_collection / 100000)) % 10 != 1:
        print("badge6")

        if history_instance.distance > 5:
            duration_seconds = history_instance.duration.total_seconds()
            print(history_instance.duration)
            print(duration_seconds)
            if duration_seconds != 0:
                avg_speed = history_instance.distance / (duration_seconds / 3600)
            else:
                avg_speed = 0
            if avg_speed > 12:
                badge_collection += 100000

    if ((int)(badge_collection / 1000000)) % 10 != 1:
        print("badge7")

        if history_instance.distance >= 21:
            badge_collection += 1000000
    if ((int)(badge_collection / 10000000)) % 10 != 1:
        print("badge8")

        if history_instance.distance >= 42.195:
            badge_collection += 10000000

    if ((int)(badge_collection / 100000000)) % 10 != 1:
        print("badge9")

        if history_instance.distance >= 42.195 and history_instance.is_group is True:
            group_history_instance = GroupHistoryRecord.objects.get(
                id=history_instance.group_history_id
            )
            if group_history_instance.first_place_user_id == history_instance.user_id:
                badge_collection += 100000000

    print("return badge_collection :", badge_collection)
    return badge_collection
