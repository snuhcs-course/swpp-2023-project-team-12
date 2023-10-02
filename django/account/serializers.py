from rest_framework import serializers
from .models import CustomUser

class UserCreateSerializer(serializers.ModelSerializer):
    def create(self, validated_data):
        user: CustomUser = CustomUser.objects.create_user(**validated_data)
        return user

    class Meta:
        model = CustomUser
        fields = ['username', 'nickname', 'phone_num','email', 'password']


