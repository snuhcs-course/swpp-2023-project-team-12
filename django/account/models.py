from django.db import models
from django.contrib.auth.models import AbstractBaseUser, PermissionsMixin
from django.contrib.auth.models import UserManager

class MyUserManager(UserManager):
    def create_user(self, nickname, username, email, phone_num, password, gender, height, weight, age, **extra_fields):
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
    is_staff = models.BooleanField(default=False)
    is_superuser = models.BooleanField(default=False)
    USERNAME_FIELD = 'username'

    def __str__(self):
        return self.username

# class UserProfile(models.Model):
#     user = models.OneToOneField(CustomUser, on_delete=models.CASCADE)
#     max_speed = models.IntegerField(default=6)
#     level = models.IntegerField(default=1)
#     exp = models.FloatField(default=0)

#     def __str__(self):
#         return f"id={self.id}, user_id={self.user.id}, nickname={self.nickname}, phone_num={self.phone_num}, max_speed={self.max_speed}, level={self.level}, exp={self.exp}"
