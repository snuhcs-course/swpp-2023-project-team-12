from django.db import models
from django.contrib.auth.models import User

class UserProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    nickname = models.CharField(max_length=10, blank=False)
    phone_num = models.CharField(max_length=11, blank=False)
    max_speed = models.IntegerField(default=6)
    level = models.IntegerField(default=1)
    exp = models.FloatField(default=0)

    def __str__(self):
        return f"id={self.id}, user_id={self.user.id}, nickname={self.nickname}, phone_num={self.phone_num}, max_speed={self.max_speed}, level={self.level}, exp={self.exp}"
