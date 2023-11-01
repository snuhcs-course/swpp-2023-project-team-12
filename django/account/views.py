from .serializers import UserCreateSerializer, LoginSerializer
from rest_framework.response import Response
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer
from rest_framework.views import APIView
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response


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
