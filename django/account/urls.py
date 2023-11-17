from django.urls import path
from .views import (
    FindUsernameAndSendEmailView,
    ProfileImageView,
    ResetPasswordView,
    SignupView,
    LoginView,
    UserProfileView,
)
from . import views

appname = "accounts"

urlpatterns = [
    path("signup/", SignupView.as_view(), name="signup"),
    path("login/", LoginView.as_view(), name="login"),
    path("get-user-info/", views.get_user_info, name="get_user_info"),
    path(
        "find_username/", FindUsernameAndSendEmailView.as_view(), name="find_username"
    ),
    path("reset_password/", ResetPasswordView.as_view(), name="reset_password"),
    path("profile_image/", ProfileImageView.as_view(), name="profile_image"),
    path(
        "user_profile/<int:user_id>/",
        UserProfileView.as_view(),
        name="user_profile",
    ),
]
