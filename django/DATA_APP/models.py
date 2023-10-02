from django.db import models

# Create your models here.
class Test(models.Model):
  test = models.CharField(max_length=10)

  def __str__(self):
    return self.test