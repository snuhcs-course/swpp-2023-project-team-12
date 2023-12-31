from django.urls import path
from .views import (
    HistoryDetail,
    GroupHistoryDetail,
    MonthlyDataView,
    RecentHistory,
)

app_name = "history/"

urlpatterns = [
    path("", HistoryDetail.as_view(), name="history_detail"),
    path(
        "recent-history/<int:user_id>/", RecentHistory.as_view(), name="recent_history"
    ),
    path("group/", GroupHistoryDetail.as_view(), name="group_history_detail"),
    path("monthly/<int:year>/<int:month>/<int:user_id>/", MonthlyDataView.as_view()),
]
