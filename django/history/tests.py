from datetime import timedelta, datetime
from django.test import TestCase
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APIClient
from history.models import HistoryRecord
from account.models import CustomUser


class HistoryDetailTestCase(TestCase):
    def setUp(self):
        self.client = APIClient()

    def test_create_history(self):
        url = "/history/"
        data = {
            "user_id": 9,
            "distance": 10.0,
            "duration": 3600,
            "is_completed": True,
            "start_time": "2023-11-03T13:06:33",
            "finish_time": "2023-11-03T13:06:33",
            "calories": 500,
            "is_group": False,
            "max_speed": 15.0,
            "min_speed": 5.0,
            "median_speed": 10.0,
            "sectional_speed": "[8.0, 12.0, 10.0]",
            "group_history_id": None,
            "is_mission_succeeded": 2,
            "exp": 5,
        }
        response = self.client.post(url, data, format="json")
        if CustomUser.objects.filter(id=data.get("user_id")).last() is not None:
            self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        else:
            self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)


class HistoryListTestCase(TestCase):
    def setUp(self):
        self.client = APIClient()

    def test_get_recent_history(self):
        userid = 1
        url = reverse("history/:recent_history", args=[1])
        response = self.client.get(url, format="json")
        self.assertEqual(response.status_code, status.HTTP_200_OK)


class GroupHistoryDetailTestCase(TestCase):
    def setUp(self):
        self.client = APIClient()

    def test_create_group_history(self):
        url = "/history/group/"
        data = {
            "roomname": "Room1",
            "start_time": "2023-11-03T13:06:33",
            "duration": 3600,
            "num_players": 3,
            "first_place_user_id": 1,
            "first_place_user_distance": 15.0,
            "second_place_user_id": 2,
            "second_place_user_distance": 12.0,
            "third_place_user_id": 3,
            "third_place_user_distance": 10.0,
        }
        response = self.client.post(url, data, format="json")
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)


class MonthlyDataViewTest(TestCase):
    def setUp(self):
        self.client = APIClient()
        self.user_id = 123  # Example user_id
        self.year = 2023
        self.month = 1  # Example month

        for day in range(1, 31):
            HistoryRecord.objects.create(
                user_id=self.user_id,
                distance=5.0,
                duration=timedelta(hours=1),
                calories=300.0,
                start_time=datetime(self.year, self.month, day, 12, 0),
                finish_time=datetime(self.year, self.month, day, 13, 0),
                is_completed=True,
                is_group=False,
            )

    def test_monthly_data_with_valid_params(self):
        url = f"/history/monthly/{self.year}/{self.month}/{self.user_id}/"
        response = self.client.get(url, format="json")
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn("total_distance", response.data)
        self.assertIn("total_time", response.data)
        self.assertIn("total_calories", response.data)
        self.assertIn("daily_data", response.data)

    def test_monthly_data_with_invalid_user(self):
        url = f"/history/monthly/{self.year}/{self.month}/-1/"
        response = self.client.get(url, format="json")
        self.assertNotEqual(response.status_code, status.HTTP_200_OK)
