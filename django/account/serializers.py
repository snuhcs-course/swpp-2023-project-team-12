from rest_framework import serializers
from .models import CustomUser
from django.contrib.auth.hashers import check_password
from django.contrib.auth import authenticate

class UserCreateSerializer(serializers.ModelSerializer):
    def create(self, validated_data):
        user: CustomUser = CustomUser.objects.create_user(**validated_data)
        return user

    class Meta:
        model = CustomUser
        fields = ['username', 'nickname', 'email', 'phone_num','password']


class LoginSerializer(serializers.Serializer):

    username = serializers.CharField(max_length=255, write_only=True)
    password = serializers.CharField(max_length=128, write_only=True)

    def validate(self, data):
        username = data.get('username', None)
        password = data.get('password', None)

        if username is None:
            raise serializers.ValidationError(
                'An username should be included to log in.'
            )
        
        if password is None:
            raise serializers.ValidationError(
                'A password should be included to log in.'
            )

        if username and password:
            user = authenticate(
                username=username, 
                password=password
                )
            if user is None:
                raise serializers.ValidationError("Login error")
        else:
            raise serializers.ValidationError("Must include Username and password")
        
        data['user'] = user
        return data
    


            



