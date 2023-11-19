import logging
from rest_framework import serializers
from .models import CustomUser
from django.contrib.auth import authenticate
from django.core.files.storage import default_storage

logger = logging.getLogger(__name__)


class UserCreateSerializer(serializers.ModelSerializer):
    def create(self, validated_data):
        user: CustomUser = CustomUser.objects.create_user(**validated_data)
        return user

    class Meta:
        model = CustomUser
        fields = [
            "username",
            "nickname",
            "email",
            "phone_num",
            "password",
            "gender",
            "height",
            "weight",
            "age",
            
        ]


class LoginSerializer(serializers.Serializer):
    username = serializers.CharField(max_length=255, write_only=True)
    password = serializers.CharField(max_length=128, write_only=True)

    def validate(self, data):
        username = data.get("username", None)
        password = data.get("password", None)

        if username is None:
            raise serializers.ValidationError(
                "An username should be included to log in."
            )

        if password is None:
            raise serializers.ValidationError(
                "A password should be included to log in."
            )

        user = authenticate(username=username, password=password)
        if user is None:
            raise serializers.ValidationError("Login error")

        data["user"] = user
        return data


class UserProfileImageSerializer(serializers.ModelSerializer):
    class Meta:
        model = CustomUser
        fields = ["profile_image"]

    def update(self, instance, validated_data):
        new_image = validated_data.get("profile_image")

        if new_image and instance.profile_image:
            if default_storage.exists(instance.profile_image.name):
                default_storage.delete(instance.profile_image.name)
                logger.debug(f"Deleted old image: {instance.profile_image.name}")

        if new_image:
            logger.debug(f"Updating profile image to: {new_image}")
            instance.profile_image = new_image
        else:
            logger.debug("No new image provided; keeping the existing image")

        instance.save()
        logger.debug(
            f"Updated user instance: {instance.username} with image: {instance.profile_image}"
        )
        return instance


class EmailSerializer(serializers.Serializer):
    email = serializers.EmailField()


class ResetPasswordSerializer(serializers.Serializer):
    username = serializers.CharField(required=True)
    email = serializers.EmailField(required=True)


class UserProfileSerializer(serializers.ModelSerializer):
    profile_image_url = serializers.SerializerMethodField()

    class Meta:
        model = CustomUser
        fields = ["username", "badge_collection", "profile_image_url"]

    def get_profile_image_url(self, obj):
        request = self.context.get("request")
        if obj.profile_image and hasattr(obj.profile_image, "url"):
            return request.build_absolute_uri(obj.profile_image.url)
        return None
