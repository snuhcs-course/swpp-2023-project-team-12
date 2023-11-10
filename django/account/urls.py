from django.urls import path
from .views import FindUsernameAndSendEmailView, SignupView, LoginView
from . import views

appname = "accounts"

urlpatterns = [
    path("signup/", SignupView.as_view(), name="signup"),
    path("login/", LoginView.as_view(), name="login"),
    path("get-user-info/", views.get_user_info, name="get_user_info"),
    path(
        "find_username/", FindUsernameAndSendEmailView.as_view(), name="find_username"
    ),
]
