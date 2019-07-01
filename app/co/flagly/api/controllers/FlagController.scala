package co.flagly.api.controllers

import java.util.UUID

import co.flagly.api.auth.AccountCtx
import co.flagly.api.models.flagWrites
import co.flagly.api.services.{AccountService, FlagService}
import co.flagly.api.views.{CreateFlag, UpdateFlag}
import co.flagly.core.FlaglyError
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FlagController(flagService: FlagService, accountService: AccountService, cc: ControllerComponents) extends BaseController(cc) {
  def create(applicationId: UUID): Action[CreateFlag] =
    accountActionWithBody[CreateFlag](accountService) { ctx: AccountCtx[CreateFlag] =>
      flagService.create(applicationId, ctx.request.body).map { flag =>
        resultAsJson(flag, Created)
      }
    }

  def get(applicationId: UUID, name: Option[String]): Action[AnyContent] =
    accountAction(accountService) { _: AccountCtx[AnyContent] =>
      name match {
        case None =>
          flagService.getAll(applicationId).map(flags => resultAsJson(flags))

        case Some(n) =>
          flagService.getByName(applicationId, n).flatMap {
            case None       => Future.failed(FlaglyError.of(s"Flag '$n' of application $applicationId does not exist!"))
            case Some(flag) => Future.successful(resultAsJson(flag))
          }
      }
    }

  def getById(applicationId: UUID, flagId: UUID): Action[AnyContent] =
    accountAction(accountService) { _: AccountCtx[AnyContent] =>
      flagService.get(applicationId, flagId).flatMap {
        case None       => Future.failed(FlaglyError.of(s"Flag '$flagId' does not exist!"))
        case Some(flag) => Future.successful(resultAsJson(flag))
      }
    }

  def update(applicationId: UUID, flagId: UUID): Action[UpdateFlag] =
    accountActionWithBody[UpdateFlag](accountService) { ctx: AccountCtx[UpdateFlag] =>
      flagService.update(applicationId, flagId, ctx.request.body).map { flag =>
        resultAsJson(flag)
      }
    }

  def delete(applicationId: UUID, flagId: UUID): Action[AnyContent] =
    accountAction(accountService) { _: AccountCtx[AnyContent] =>
      flagService.delete(applicationId, flagId).map { _ =>
        Ok
      }
    }
}
