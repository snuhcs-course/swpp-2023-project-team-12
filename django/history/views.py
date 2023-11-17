from .serializers import (
    DailyDataSerializer,
    HistorySerializer,
    GroupHistorySerializer,
    MonthlyDataSerializer,
    RecentHistorySerializer,
)
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework import status
from .models import history, group_history
from datetime import datetime, timedelta
from django.utils import timezone
from django.db.models import Sum
from django.db.models.functions import TruncDay
from venv import logger
from account.models import CustomUser


# Create your views here.
class HistoryDetail(APIView):
    def post(self, request):
        user_id = request.data.get("user_id")
        distance = request.data.get("distance")
        duration_in_seconds = request.data.get("duration")
        duration = timedelta(seconds=duration_in_seconds)
        print(duration)
        is_completed = request.data.get("is_completed")
        start_time_str = request.data.get("start_time")
        start_time = datetime.fromisoformat(start_time_str)
        start_time = timezone.make_aware(start_time)
        finish_time_str = request.data.get("finish_time")
        finish_time = datetime.fromisoformat(finish_time_str)
        finish_time = timezone.make_aware(finish_time)
        calories = request.data.get("calories")
        is_group = request.data.get("is_group")
        max_speed = request.data.get("max_speed")
        min_speed = request.data.get("min_speed")
        median_speed = request.data.get("median_speed")
        sectional_speed = request.data.get("sectional_speed")
        group_history_id = request.data.get("group_history_id")
        is_mission_succeeded = request.data.get("is_mission_succeeded")
        exp = request.data.get("exp")
        history_instance = history.objects.create(
            user_id=user_id,
            distance=distance,
            duration=duration,
            is_completed=is_completed,
            start_time=start_time,
            finish_time=finish_time,
            calories=calories,
            is_group=is_group,
            max_speed=max_speed,
            min_speed=min_speed,
            median_speed=median_speed,
            sectional_speed=sectional_speed,
            group_history_id=group_history_id,
            is_mission_succeeded=is_mission_succeeded,
        )
        user = CustomUser.objects.filter(id = user_id).last()
        if user is not None:
            user.exp = user.exp + exp
            user.save()
        else:
            return Response({"message": "User not found"}, status=404)

        serializer = HistorySerializer(history_instance)
        print({"history" : serializer.data, "exp" : user.exp})
        return Response({"history" : serializer.data, "exp" : {"exp" : user.exp}}, status=status.HTTP_201_CREATED)


class RecentHistory(APIView):
    def get(self, request, user_id):
        try:
            user_history = history.objects.filter(user_id=user_id).order_by(
                "-start_time"
            )[:5]
            serializer = RecentHistorySerializer(user_history, many=True)
            print(serializer.data)

            return Response(serializer.data, status=status.HTTP_200_OK)
        except Exception as e:
            return Response(
                {"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )


class GroupHistoryDetail(APIView):
    def post(self, request):
        roomname = request.data.get("roomname")
        start_time_str = request.data.get("start_time")
        start_time = datetime.fromisoformat(start_time_str)
        start_time = timezone.make_aware(start_time)
        duration_in_seconds = request.data.get("duration")
        duration = timedelta(seconds=duration_in_seconds)
        num_players = request.data.get("num_players")
        first_place_user_id = request.data.get("first_place_user_id")
        first_place_user_distance = request.data.get("first_place_user_distance")
        second_place_user_id = request.data.get("second_place_user_id")
        second_place_user_distance = request.data.get("second_place_user_distance")
        third_place_user_id = request.data.get("third_place_user_id")
        third_place_user_distance = request.data.get("third_place_user_distance")
        group_history_instance = group_history.objects.create(
            roomname=roomname,
            start_time=start_time,
            duration=duration,
            num_players=num_players,
            first_place_user_id=first_place_user_id,
            first_place_user_distance=first_place_user_distance,
            second_place_user_id=second_place_user_id,
            second_place_user_distance=second_place_user_distance,
            third_place_user_id=third_place_user_id,
            third_place_user_distance=third_place_user_distance,
        )
        serializer = GroupHistorySerializer(group_history_instance)
        print(serializer.data)
        return Response(serializer.data, status=status.HTTP_201_CREATED)


class MonthlyDataView(APIView):
    def get(self, request, year, month, user_id):
        start_date = datetime(year, month, 1).date()
        end_date = (
            datetime(year, month + 1, 1).date()
            if month < 12
            else datetime(year + 1, 1, 1).date()
        )
        data = history.objects.filter(
            user_id=user_id, start_time__range=[start_date, end_date]
        )
        total_distance = data.aggregate(Sum("distance"))["distance__sum"] or 0
        total_time = data.aggregate(Sum("duration"))["duration__sum"] or timedelta(0)
        total_calories = data.aggregate(Sum("calories"))["calories__sum"] or 0

        daily_data = (
            data.annotate(date=TruncDay("start_time"))
            .values("date")
            .annotate(
                distance=Sum("distance"), time=Sum("duration"), calories=Sum("calories")
            )
            .order_by("date")
        )
        for item in daily_data:
            item["date"] = item["date"].date()
            logger.debug(daily_data)
        response_data = {
            "total_distance": total_distance,
            "total_time": total_time,
            "total_calories": total_calories,
            "daily_data": list(daily_data),
        }

        logger.debug(f"Response data before serialization: {response_data}")

        serializer = MonthlyDataSerializer(response_data)
        serialized_data = serializer.data

        logger.debug(f"Serialized data: {serialized_data}")
        return Response(serializer.data)


# NOTE: 잠시 Deperecated된 API
class DailyDataView(APIView):
    def get(self, request, year, month, day, user_id):
        data = history.objects.filter(
            user_id=user_id,
            start_time__year=year,
            start_time__month=month,
            start_time__day=day,
        )
        total_distance = data.aggregate(Sum("distance"))["distance__sum"] or 0
        total_time = data.aggregate(Sum("duration"))["duration__sum"] or timedelta(0)
        total_calories = data.aggregate(Sum("calories"))["calories__sum"] or 0
        serializer = DailyDataSerializer(
            {"distance": total_distance, "time": total_time, "calories": total_calories}
        )
        return Response(serializer.data)
