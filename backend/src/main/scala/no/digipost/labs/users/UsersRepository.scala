package no.digipost.labs.users

import java.util.Date

trait UsersRepository {

  def findById(id: String): Option[DbUser]

  def findByDigipostId(id: String): Option[DbUser]

  def findByOpenId(id: String): Option[DbUser]

  def insert(user: DbUser): Option[DbUser]

  def update(user: DbUser, id: String): Option[DbUser]

  def updateLastLogin(id: String, date: Date): Unit

  def updateProfile(profile: DbProfile, userId: String): Option[DbUser]

  def getUsersWithProfile: Seq[DbUser]

}
