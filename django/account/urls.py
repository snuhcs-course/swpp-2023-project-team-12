from django.urls import path
from .views import SignupView

appname = 'accounts'

urlpatterns = [
    # ... 다른 패턴들 ...
    path('signup/', SignupView.as_view(), name='signup'),
]