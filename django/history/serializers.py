from rest_framework import serializers
from .models import history, group_history

class HistorySerializer(serializers.ModelSerializer):
    class Meta:
        model = history
        fields = '__all__'

class GroupHistorySerializer(serializers.ModelSerializer):
    class Meta:
        model = group_history
        fields = '__all__'