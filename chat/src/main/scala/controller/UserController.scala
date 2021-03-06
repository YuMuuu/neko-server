package neko.chat.controller

import java.time.Clock

import neko.core.http.{HttpRequest, HttpResponse}
import neko.core.http.{OK, BAD_REQUEST, CONFLICT, INTERNAL_SERVER_ERROR}
import neko.core.json.Json
import neko.core.jdbc.DBPool

import neko.chat.repository.AuthRepository.UserNotExistOrDuplicateUserNameException
import neko.chat.service.UserCreateService
import neko.chat.service.UserCreateService.UserCreateRequest
import neko.chat.entity.User
import neko.core.json.{JsonDecoder, JsonEncoder}
import neko.core.json.JsValue

class UserController(
    userCreateService: UserCreateService,
    dbPool: DBPool,
    clock: Clock
) {

  import UserController._

  def create(request: HttpRequest): HttpResponse = {
    val result: Either[HttpResponse, HttpResponse] = for {
      userCreateRequest <- Json
        .parse(request.body)
        .flatMap(userCreateRequestDecoder.decode)
        .toRight(HttpResponse(BAD_REQUEST))
      user <- userCreateService.create(userCreateRequest).runTx(dbPool.getConnection()).left.map {
        case e: UserNotExistOrDuplicateUserNameException => HttpResponse(CONFLICT, "loginNameが既に使われています")
        case _                                           => HttpResponse(INTERNAL_SERVER_ERROR)
      }
    } yield {
      val jsonString = Json.format(userEncoder.encode(user))
      HttpResponse(OK, jsonString).withContentType("application/json")
    }
    result.merge
  }

}

object UserController {

  val userCreateRequestDecoder: JsonDecoder[UserCreateRequest] = new JsonDecoder[UserCreateRequest] {
    override def decode(js: JsValue): Option[UserCreateRequest] = {
      for {
        screenName  <- (js \ "screenName").as[String]
        loginName   <- (js \ "loginName").as[String]
        rawPassword <- (js \ "password").as[String]
      } yield UserCreateRequest(screenName, loginName, rawPassword)
    }
  }

  val nameDecoder: JsonDecoder[String] = new JsonDecoder[String] {
    override def decode(js: JsValue): Option[String] = {
      (js \ "name").as[String]
    }
  }

  val userEncoder: JsonEncoder[User] = new JsonEncoder[User] {
    override def encode(user: User): JsValue = Json.obj(
      "id"          -> Json.str(user.id.toString),
      "screen_name" -> Json.str(user.screenName)
    )
  }

}
