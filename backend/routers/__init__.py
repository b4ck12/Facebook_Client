"""
Routers package for Facebook Client Backend API.
"""
from . import upload
from . import users
from . import posts
from . import friends
from . import comments
from . import notifications

__all__ = ["upload", "users", "posts", "friends", "comments", "notifications"]
