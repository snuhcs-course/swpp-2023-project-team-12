from rest_framework import serializers
from .models import CustomUser
from django.contrib.auth.hashers import check_password

class UserCreateSerializer(serializers.ModelSerializer):
    def create(self, validated_data):
        user: CustomUser = CustomUser.objects.create_user(**validated_data)
        return user

    class Meta:
        model = CustomUser
        fields = ['username', 'nickname', 'email', 'phone_num','password']


class LoginSerializer(serializers.ModelSerializer):
    def validate(self, data):

        username = data.get('username', None)
        password = data.get('password', None)


        if not CustomUser.objects.filter(username=username).exists():
            raise serializers.ValidationError("No user exists")
        
        user = CustomUser.objects.get(username=username)
        
        if not check_password(password, user.password):
            raise serializers.ValidationError("Password Error")
        
        return {"user":user}
    
    class Meta:
        model = CustomUser
        fields = ['username', 'password']

            



