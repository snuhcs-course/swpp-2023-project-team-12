import os
from django.conf import settings
from django.test import TestCase
from django.urls import reverse
from django.core.files.uploadedfile import SimpleUploadedFile
from django.contrib.auth import get_user_model
from rest_framework.test import APIClient
from rest_framework_simplejwt.tokens import AccessToken


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
        """DB에 저장되는지 확인"""
        # 업로드할 이미지 파일 생성
        current_dir = os.getcwd()
        image_path = "./media/profile_images/temp_profile.jpeg"  # 테스트용 이미지 파일 경로
        image = SimpleUploadedFile(
            name="test_image.jpg",
            content=open(image_path, "rb").read(),
            content_type="image/jpeg",
        )

        # API 엔드포인트 호출
        url = reverse("profile_image")  # 'profile_image'는 URL 패턴의 이름
        response = self.client.post(url, {"profile_image": image}, format="multipart")

        # 응답 코드 검사
        self.assertEqual(response.status_code, 200)

        # 데이터베이스에 업로드된 이미지가 저장되었는지 검사
        self.user.refresh_from_db()
        self.assertTrue(self.user.profile_image.url.endswith("test_image.jpg"))

    def tearDown(self):
        # 데이터베이스 정리
        self.user.delete()
