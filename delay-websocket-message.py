# coding: utf-8

import asyncio
import logging
import json

from mitmproxy import http
from mitmproxy import ctx

LOG_TAG = 'DelayWebSocketMessageAddon'

async def websocket_message(flow: http.HTTPFlow):
    global pending_message_from_server

    assert flow.websocket is not None

    # Webソケットにおける最新のメッセージを取得
    latest_message = flow.websocket.messages[-1]

    if latest_message.from_client:
        # クライアントから送信されたメッセージは、特に何もしない
        logging.info(f"[{LOG_TAG}] Received a message from client: {latest_message.text}")
    else:
        # サーバーから送信されたメッセージに対して、特定のキーワードが含まれていたら、遅延処理を行う
        # また、スクリプト内で生成したメッセージに対して再起的に同じ処理を繰り返さないよう、`injected` フラグで識別
        if not latest_message.injected and latest_message.text == '1':
            logging.info(f"[{LOG_TAG}] Will delay a message from server: {latest_message.text}")

            # サーバーから送信された元々のメッセージをキャンセルする
            latest_message.drop()

            # サーバーから送信されたメッセージと同一のメッセージを、遅延させた後に再送する
            # websocket_message の処理をブロックすると、後続の他メッセージも送信されずに止まってしまうため、非同期で処理する
            asyncio.create_task(post_websocket_message_async(flow, latest_message.text))
        else:
            logging.info(f"[{LOG_TAG}] Received a message from server: {latest_message.text}")


async def post_websocket_message_async(flow: http.HTTPFlow, message: str):
    await asyncio.sleep(3)

    to_client = True
    # サーバーから送信されたメッセージと同じものをクライアントに再送信する
    ctx.master.commands.call("inject.websocket", flow, to_client, message.encode())

    logging.info(f"[{LOG_TAG}] Send the delayed message from server: {message}")
