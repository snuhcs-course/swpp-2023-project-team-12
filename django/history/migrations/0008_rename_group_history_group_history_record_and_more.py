# Generated by Django 4.2.5 on 2023-11-19 11:04

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ("history", "0007_history_is_mission_succeeded"),
    ]

    operations = [
        migrations.RenameModel(
            old_name="group_history", new_name="group_history_record",
        ),
        migrations.RenameModel(old_name="history", new_name="history_record",),
    ]
