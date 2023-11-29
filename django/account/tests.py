from django.test import TestCase
from django.urls import reverse
from django.core.files.uploadedfile import SimpleUploadedFile
from django.contrib.auth import get_user_model
from rest_framework.test import APIClient
from rest_framework import status
from rest_framework_simplejwt.tokens import AccessToken
from django.core.files.storage import default_storage

from PIL import Image
from io import BytesIO
from django.core.files.uploadedfile import SimpleUploadedFile


def generate_dummy_image():
    image = Image.new("RGB", (100, 100), (255, 255, 255))
    image_io = BytesIO()
    image.save(image_io, format="JPEG")
    image_io.seek(0)
    return SimpleUploadedFile(
        "dummy.jpg", image_io.getvalue(), content_type="image/jpeg"
    )


class ProfileImageUploadTest(TestCase):
    """이미지 업로드 테스트"""

    def setUp(self):
        # 테스트 사용자 생성
        self.user = get_user_model().objects.create_user(
            username="testuser",
            password="testpassword",
            nickname="testnickname",
            email="testemail@test.test",
            phone_num="01011112222",
            gender="1",
            height="60",
            weight="170",
            age="23",
        )
        self.client = APIClient()
        access = AccessToken.for_user(self.user)
        self.client.credentials(HTTP_AUTHORIZATION=f"Bearer {access}")
        self.client.login(username="testuser", password="testpassword")

    def test_profile_image_upload(self):
        # 더미 이미지 생성
        image = generate_dummy_image()

        # API 엔드포인트 호출
        url = reverse("profile_image")
        response = self.client.post(url, {"profile_image": image}, format="multipart")

        # 응답 코드 검사
        self.assertEqual(response.status_code, 200)

        # 데이터베이스에 업로드된 이미지 검증
        self.user.refresh_from_db()
        self.assertTrue(self.user.profile_image.url.endswith("dummy.jpg"))

    def tearDown(self):
        # 데이터베이스 정리
        self.user.delete()

        # 업로드된 더미 이미지 파일 삭제
        if self.user.profile_image and default_storage.exists(
            self.user.profile_image.name
        ):
            default_storage.delete(self.user.profile_image.name)

        super().tearDown()


class SignupViewTest(TestCase):
    def setUp(self):
        self.client = APIClient()

    def test_signup_with_valid_data(self):
        valid_data = {
            "username": "newuser",
            "password": "newpassword",
            "email": "newuser@example.com",
            "nickname": "Djangotest",
            "phone_num": "01000000000",
            "gender": 1,
            "height": 170,
            "weight": 70,
            "age": 25,
        }
        response = self.client.post(reverse("signup"), valid_data, format="json")
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertIn("name", response.data)

    def test_signup_with_invalid_data(self):
        invalid_data = {
            "username": "",
            "password": "",
            # 유효하지 않은 데이터
        }
        response = self.client.post(reverse("signup"), invalid_data, format="json")
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)


class ResetPasswordViewTest(TestCase):
    def setUp(self):
        self.client = APIClient()
        self.user = get_user_model().objects.create_user(
            username="testuser",
            password="testpassword",
            email="test@test.com",
            nickname="Djangotest",
            phone_num="01000000000",
            gender=1,
            height=170,
            weight=70,
            age=25,
        )

    def test_reset_password_with_valid_info(self):
        response = self.client.post(
            reverse("reset_password"),
            {"username": "testuser", "email": "test@test.com"},
            format="json",
        )
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    def test_reset_password_with_invalid_info(self):
        response = self.client.post


class FindUsernameAndSendEmailViewTest(TestCase):
    def setUp(self):
        self.client = APIClient()
        self.user = get_user_model().objects.create_user(
            username="testuser",
            password="testpassword",
            email="test@test.com",
            nickname="Djangotest",
            phone_num="01000000000",
            gender=1,
            height=170,
            weight=70,
            age=25,
        )

    def test_send_email_with_existing_email(self):
        response = self.client.post(
            reverse("find_username"), {"email": "test@test.com"}, format="json"
        )
        self.assertEqual(response.status_code, status.HTTP_200_OK)


class LoginViewTest(TestCase):
    def setUp(self):
        self.client = APIClient()
        self.user = get_user_model().objects.create_user(
            username="testuser",
            password="testpassword",
            email="test@test.com",
            nickname="Djangotest",
            phone_num="01000000000",
            gender=1,
            height=170,
            weight=70,
            age=25,
        )

    def test_login_with_valid_credentials(self):
        response = self.client.post(
            reverse("login"),
            {"username": "testuser", "password": "testpassword"},
            format="json",
        )
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn("jwt_token", response.data)
