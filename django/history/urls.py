from django.urls import path
from .views import HistoryDetail, GroupHistoryDetail

app_name = "history/"

urlpatterns = [
  path("", HistoryDetail.as_view(), name="history_detail"),
  path("group/", GroupHistoryDetail.as_view(), name = "group_history_detail")
]