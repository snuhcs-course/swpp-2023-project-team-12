from django.urls import path
from .views import SignupView, LoginView

appname = 'accounts'

urlpatterns = [
    # ... 다른 패턴들 ...
    path('signup/', SignupView.as_view(), name='signup'),
    path('login/', LoginView.as_view(), name='login'),
]