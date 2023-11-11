from rest_framework import serializers
from .models import history, group_history


class HistorySerializer(serializers.ModelSerializer):
    class Meta:
        model = history
        fields = "__all__"


class GroupHistorySerializer(serializers.ModelSerializer):
    class Meta:
        model = group_history
        fields = "__all__"


class RecentHistorySerializer(serializers.ModelSerializer):
    class Meta:
        model = history
        fields = ("distance", "duration")


class DailyDataSerializer(serializers.Serializer):
    date = serializers.DateField()
    distance = serializers.FloatField()
    time = serializers.DurationField()
    calories = serializers.FloatField()


class MonthlyDataSerializer(serializers.Serializer):
    total_distance = serializers.FloatField()
    total_time = serializers.DurationField()
    total_calories = serializers.FloatField()
    daily_data = DailyDataSerializer(many=True)
