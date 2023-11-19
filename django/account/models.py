from django.db import models
from django.contrib.auth.models import AbstractBaseUser, PermissionsMixin
from django.contrib.auth.models import UserManager


class MyUserManager(UserManager):
    def create_user(
        self,
        nickname,
        username,
        email,
        phone_num,
        password,
        gender,
        height,
        weight,
        age,
        **extra_fields
    ):
        if not nickname:
            raise ValueError("SET USERID")
        if not username:
            raise ValueError("SET USERNAME")
        if not password:
            raise ValueError("SET PASSWORD")
        email = self.normalize_email(email)
        user = self.model(
            nickname=nickname,
            username=username,
            phone_num=phone_num,
            email=email,
            gender=gender,
            height=height,
            weight=weight,
            age=age,
            exp=0,
            badge_collection=1000000000,
            **extra_fields
        )
        user.set_password(password)
        user.save()

        return user

class CustomUser(AbstractBaseUser, PermissionsMixin):
    objects = MyUserManager()
    username = models.CharField(null=False, max_length=100, unique=True)
    nickname = models.CharField(null=False, max_length=100, unique=True)
    email = models.EmailField(max_length=50, null=True)
    phone_num = models.CharField(max_length=11, blank=False)
    gender = models.IntegerField(default=3)
    height = models.FloatField(default=0)
    weight = models.FloatField(default=0)
    age = models.IntegerField(default=20)
    exp = models.IntegerField(default=0)
    badge_collection = models.IntegerField(null=False, default=1000000000)
    is_staff = models.BooleanField(default=False)
    is_superuser = models.BooleanField(default=False)
    profile_image = models.ImageField(
        upload_to="profile_images",
        max_length=500,
        default="profile_images/temp_profile.jpeg",
    )
    USERNAME_FIELD = "username"

    def __str__(self):
        return self.username

