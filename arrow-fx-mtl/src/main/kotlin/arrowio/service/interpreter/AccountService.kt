package arrowio.service.interpreter

import arrow.core.*
import arrow.fx.IO
import arrow.mtl.EitherT
import arrow.mtl.Kleisli
import arrowio.Amount
import arrowio.ErrorOr
import arrowio.model.Account
import arrowio.model.Balance
import arrowio.repository.AccountRepository
import arrowio.service.*
import arrowio.service.AccountService
import arrowio.today
import java.math.BigDecimal
import java.time.LocalDate

object AccountService : AccountService<Account, Amount, Balance> {

    override fun open(
        no: String,
        name: String,
        rate: Option<BigDecimal>,
        openingDate: Option<LocalDate>,
        accountType: AccountType
    ): AccountOperation<Account> = Kleisli.invoke { repo ->
        EitherT(
            repo.query(no).flatMap { accountOptionOrError ->
                accountOptionOrError.fold(
                    { IO { MiscellaneousDomainExceptions(it).left() } },
                    { accOpt ->
                        accOpt.fold(
                            {
                                when (accountType) {
                                    AccountType.CHECKING -> createOrUpdate(
                                        repo,
                                        Account.checkingAccount(no, name, openingDate, None, Balance())
                                    )
                                    AccountType.SAVINGS -> rate.map { r ->
                                        createOrUpdate(
                                            repo,
                                            Account.savingsAccount(no, name, r, openingDate, None, Balance())
                                        )
                                    }.getOrElse { IO { RateMissingForSavingsAccount.left() } }
                                }
                            },
                            { IO { AlreadyExistingAccount(it.no).left() } }
                        )
                    }
                )
            }
        )
    }

    private fun createOrUpdate(
        repo: AccountRepository, errorOrAccount: ErrorOr<Account>
    ): IO<Either<AccountServiceException, Account>> = when (errorOrAccount) {
        is Either.Left -> IO { MiscellaneousDomainExceptions(errorOrAccount.a).left() }
        is Either.Right -> repo.store(errorOrAccount.b).map {
            when (it) {
                is Either.Right -> it.b.right()
                is Either.Left -> MiscellaneousDomainExceptions(it.a).left()
            }
        }
    }

    override fun close(no: String, closeDate: Option<LocalDate>): AccountOperation<Account> =
        Kleisli { repo ->
            EitherT(
                repo.query(no).flatMap { accountOptionOrError ->
                    accountOptionOrError.fold(
                        { IO { MiscellaneousDomainExceptions(it).left() } },
                        { accountOption ->
                            accountOption.fold(
                                { IO { NonExistingAccount(no).left() } },
                                {
                                    val cd = closeDate.getOrElse { today() }
                                    createOrUpdate(repo, Account.close(it, cd))
                                }
                            )
                        }
                    )
                }
            )
        }

    private sealed class DC {
        object D : DC()
        object C : DC()
    }

    override fun debit(no: String, amount: Amount): AccountOperation<Account> =
        up(no, amount, DC.D)

    override fun credit(no: String, amount: Amount): AccountOperation<Account> =
        up(no, amount, DC.C)

    private fun up(no: String, amount: Amount, dc: DC): AccountOperation<Account> =
        Kleisli { repo ->
            EitherT(
                repo.query(no).flatMap { accountOptionOrError ->
                    accountOptionOrError.fold(
                        { IO { MiscellaneousDomainExceptions(it).left() } },
                        { accountOption ->
                            accountOption.fold(
                                { IO { NonExistingAccount(no).left() } },
                                {
                                    val updated = when (dc) {
                                        is DC.D -> createOrUpdate(repo, Account.updateBalance(it, -amount))
                                        is DC.C -> createOrUpdate(repo, Account.updateBalance(it, amount))
                                    }
                                    updated
                                }
                            )
                        }
                    )
                }
            )
        }

    override fun balance(no: String): AccountOperation<Balance> =
        Kleisli { repo ->
            EitherT(
                repo.balance(no).map { errorOrBalance ->
                    errorOrBalance.fold(
                        { MiscellaneousDomainExceptions(it).left() },
                        { it.right() }
                    )
                }
            )
        }
}