from django.db import models
from account.models import CustomUser

# Create your models here.
class history(models.Model):
  user_id = models.BigIntegerField(null=False)
  distance = models.FloatField(null=False, blank=False)
  duration = models.DurationField()
  is_completed = models.BooleanField(null=False, blank=False)
  start_time = models.DateTimeField(null=False, blank=False)
  finish_time = models.DateTimeField(null=False, blank=False)
  calories = models.FloatField(null=True, blank=True)
  is_group = models.BooleanField(null=False, blank=False)
  max_speed = models.FloatField(null=True, blank=True)
  min_speed = models.FloatField(null=True, blank=True)
  median_speed = models.FloatField(null=True, blank=True)
  group_history_id = models.BigIntegerField(null=True, blank=True)
  sectional_speed = models.TextField()
  is_mission_succeeded = models.IntegerField(default=0)
  
  def __str__(self):
    return self.user_id

class group_history(models.Model):
  roomname = models.TextField(null=False, blank=True)
  start_time = models.DateTimeField(null=False, blank=False)
  duration = models.DurationField()
  num_players = models.IntegerField(null=False, blank=False)
  first_place_user_id = models.IntegerField(null=True, blank=True)
  first_place_user_distance = models.FloatField(null=True, blank=True)
  second_place_user_id = models.IntegerField(null=True, blank=True)
  second_place_user_distance = models.FloatField(null=True, blank=True)
  third_place_user_id = models.IntegerField(null=True, blank=True)
  third_place_user_distance = models.FloatField(null=True, blank=True)



