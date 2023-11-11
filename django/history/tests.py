from django.test import TestCase
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APIClient
from datetime import datetime, timedelta
from django.utils import timezone
from .models import history, group_history

class HistoryDetailTestCase(TestCase):
    def setUp(self):
        self.client = APIClient()

    def test_create_history(self):
        url = "/history/"  # 해당 URL 패턴의 이름을 사용합니다.
        data = {
            'user_id': 1,
            'distance': 10.0,
            'duration': 3600,
            'is_completed': True,
            'start_time': '2023-11-03T13:06:33',
            'finish_time': '2023-11-03T13:06:33',
            'calories': 500,
            'is_group': False,
            'max_speed': 15.0,
            'min_speed': 5.0,
            'median_speed': 10.0,
            'sectional_speed': '[8.0, 12.0, 10.0]',
            'group_history_id': None,
        }
        response = self.client.post(url, data, format='json')
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)

# class HistoryListTestCase(TestCase):
#     def setUp(self):
#         self.client = APIClient()

#     def test_get_recent_history(self):
#         userid = 1
#         url = reverse('history/:recent_history', args=[1])
#         response = self.client.get(url, format='json')
#         self.assertEqual(response.status_code, status.HTTP_200_OK   )

class GroupHistoryDetailTestCase(TestCase):
    def setUp(self):
        self.client = APIClient()

    def test_create_group_history(self):
        url = "/history/group/"  # 해당 URL 패턴의 이름을 사용합니다.
        data = {
            'roomname': 'Room1',
            'start_time': '2023-11-03T13:06:33',
            'duration': 3600,
            'num_players': 3,
            'first_place_user_id': 1,
            'first_place_user_distance': 15.0,
            'second_place_user_id': 2,
            'second_place_user_distance': 12.0,
            'third_place_user_id': 3,
            'third_place_user_distance': 10.0,
        }
        response = self.client.post(url, data, format='json')
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)

