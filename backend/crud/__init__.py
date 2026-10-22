"""
CRUD package for database queries.
"""
from . import crud_user
from . import crud_post
from . import crud_friend

__all__ = ["crud_user", "crud_post", "crud_friend"]
