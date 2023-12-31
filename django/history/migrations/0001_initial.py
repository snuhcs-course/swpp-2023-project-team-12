# Generated by Django 4.2.5 on 2023-11-02 01:21

from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    initial = True

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
    ]

    operations = [
        migrations.CreateModel(
            name='history',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('distance', models.FloatField()),
                ('duration', models.DurationField()),
                ('is_completed', models.BooleanField()),
                ('start_time', models.DateTimeField()),
                ('finish_time', models.DateTimeField()),
                ('calories', models.FloatField(blank=True, null=True)),
                ('is_group', models.BooleanField()),
                ('max_speed', models.FloatField(blank=True, null=True)),
                ('min_speed', models.FloatField(blank=True, null=True)),
                ('median_speed', models.FloatField(blank=True, null=True)),
                ('sectional_record', models.JSONField()),
                ('user_id', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL)),
            ],
        ),
    ]
