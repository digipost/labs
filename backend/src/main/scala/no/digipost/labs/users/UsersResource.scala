package no.digipost.labs.users

import no.digipost.labs.DigipostLabsStack
import org.scalatra.{Ok, NotFound}
import scala.util.{Try, Success, Failure}
import no.digipost.labs.security.{AuthenticatedUserSupport, AcceptJsonOnlyFilter}
import no.digipost.labs.util.Logging
import no.digipost.labs.errorhandling.ResponseHandler

class UsersResource(userRepository: UsersRepository) extends DigipostLabsStack with AuthenticatedUserSupport with Logging with AcceptJsonOnlyFilter with ResponseHandler {

  get("/:id/profile") {
    Try(userRepository.findById(params("id")).map(Converters.dbUserToProfile)) match {
      case Success(p) => p.map(Ok(_)).getOrElse(NotFound("Not found"))
      case Failure(_) => NotFound("Not found")
    }
  }

  get("/profiles") {
    requireAdmin { _ =>
      toOkResponse(Try(userRepository.getUsersWithProfile.map(Converters.dbUserToProfile)))
    }
  }

}
