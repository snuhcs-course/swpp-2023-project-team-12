# Generated by Django 4.2.6 on 2023-11-29 09:59

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('history', '0008_rename_group_history_group_history_record_and_more'),
    ]

    operations = [
        migrations.RenameModel(
            old_name='group_history_record',
            new_name='GroupHistoryRecord',
        ),
        migrations.RenameModel(
            old_name='history_record',
            new_name='HistoryRecord',
        ),
    ]
