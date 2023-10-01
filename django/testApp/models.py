from django.db import models
from django.conf import settings
from django.utils import timezone


class Item_Info(models.Model):
    id = models.CharField(max_length=200,null=False, primary_key=True)
    category_L = models.IntegerField()
    name = models.CharField(max_length=200)
    value = models.IntegerField()
    price = models.IntegerField()


    def __str__(self):
        return self.name