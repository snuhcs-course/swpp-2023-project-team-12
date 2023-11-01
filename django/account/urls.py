from django.urls import path
from .views import SignupView, LoginView
from . import views

appname = "accounts"

urlpatterns = [
    # ... 다른 패턴들 ...
    path("signup/", SignupView.as_view(), name="signup"),
    path("login/", LoginView.as_view(), name="login"),
    path("get-user-info/", views.get_user_info, name="get_user_info"),
]
