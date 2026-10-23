"""
CRUD package for database queries.
"""
from . import crud_user
from . import crud_post
from . import crud_friend
from . import crud_notif
from . import crud_comment

__all__ = ["crud_user", "crud_post", "crud_friend", "crud_notif", "crud_comment"]
