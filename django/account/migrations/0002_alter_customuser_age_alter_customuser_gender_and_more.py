# Generated by Django 4.2.6 on 2023-10-11 14:47

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('account', '0001_initial'),
    ]

    operations = [
        migrations.AlterField(
            model_name='customuser',
            name='age',
            field=models.IntegerField(default=20),
        ),
        migrations.AlterField(
            model_name='customuser',
            name='gender',
            field=models.IntegerField(default=3),
        ),
        migrations.AlterField(
            model_name='customuser',
            name='height',
            field=models.FloatField(default=0),
        ),
        migrations.AlterField(
            model_name='customuser',
            name='weight',
            field=models.FloatField(default=0),
        ),
    ]
