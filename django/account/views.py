from django.conf import settings
from account.models import CustomUser
from .serializers import EmailSerializer, UserCreateSerializer, LoginSerializer
from rest_framework.response import Response
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer
from rest_framework.views import APIView
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from django.core.mail import send_mail


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
            user = CustomUser.objects.get(email=email)
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
