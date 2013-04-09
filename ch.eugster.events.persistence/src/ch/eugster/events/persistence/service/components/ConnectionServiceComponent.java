package ch.eugster.events.persistence.service.components;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;

import ch.eugster.events.persistence.Activator;
import ch.eugster.events.persistence.database.DatabaseConfigurer;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.persistence.model.Appliance;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.persistence.model.Category;
import ch.eugster.events.persistence.model.Compensation;
import ch.eugster.events.persistence.model.CompensationType;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseDetail;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.model.EmailAccount;
import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.model.GlobalSettings;
import ch.eugster.events.persistence.model.Guide;
import ch.eugster.events.persistence.model.GuideType;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.Membership;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.model.PersonSex;
import ch.eugster.events.persistence.model.PersonTitle;
import ch.eugster.events.persistence.model.Rubric;
import ch.eugster.events.persistence.model.SchoolClass;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.model.Teacher;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.model.Version;
import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.persistence.model.VisitAppliance;
import ch.eugster.events.persistence.model.VisitSettings;
import ch.eugster.events.persistence.model.VisitTheme;
import ch.eugster.events.persistence.model.Visitor;
import ch.eugster.events.persistence.model.ZipCode;
import ch.eugster.events.persistence.queries.AbstractEntityQuery;
import ch.eugster.events.persistence.queries.AddressGroupCategoryQuery;
import ch.eugster.events.persistence.queries.AddressGroupMemberQuery;
import ch.eugster.events.persistence.queries.AddressGroupQuery;
import ch.eugster.events.persistence.queries.AddressQuery;
import ch.eugster.events.persistence.queries.AddressSalutationQuery;
import ch.eugster.events.persistence.queries.AddressTypeQuery;
import ch.eugster.events.persistence.queries.ApplianceQuery;
import ch.eugster.events.persistence.queries.BookingQuery;
import ch.eugster.events.persistence.queries.BookingTypeQuery;
import ch.eugster.events.persistence.queries.CategoryQuery;
import ch.eugster.events.persistence.queries.CompensationQuery;
import ch.eugster.events.persistence.queries.CompensationTypeQuery;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.queries.CourseDetailQuery;
import ch.eugster.events.persistence.queries.CourseGuideQuery;
import ch.eugster.events.persistence.queries.CourseQuery;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.queries.DonationPurposeQuery;
import ch.eugster.events.persistence.queries.DonationQuery;
import ch.eugster.events.persistence.queries.EmailAccountQuery;
import ch.eugster.events.persistence.queries.FieldExtensionQuery;
import ch.eugster.events.persistence.queries.GlobalSettingsQuery;
import ch.eugster.events.persistence.queries.GuideQuery;
import ch.eugster.events.persistence.queries.GuideTypeQuery;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.queries.MemberQuery;
import ch.eugster.events.persistence.queries.MembershipQuery;
import ch.eugster.events.persistence.queries.ParticipantQuery;
import ch.eugster.events.persistence.queries.PersonQuery;
import ch.eugster.events.persistence.queries.PersonSettingsQuery;
import ch.eugster.events.persistence.queries.PersonSexQuery;
import ch.eugster.events.persistence.queries.PersonTitleQuery;
import ch.eugster.events.persistence.queries.RubricQuery;
import ch.eugster.events.persistence.queries.SchoolClassQuery;
import ch.eugster.events.persistence.queries.SeasonQuery;
import ch.eugster.events.persistence.queries.TeacherQuery;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.queries.VersionQuery;
import ch.eugster.events.persistence.queries.VisitApplianceQuery;
import ch.eugster.events.persistence.queries.VisitQuery;
import ch.eugster.events.persistence.queries.VisitSettingsQuery;
import ch.eugster.events.persistence.queries.VisitThemeQuery;
import ch.eugster.events.persistence.queries.VisitorQuery;
import ch.eugster.events.persistence.queries.ZipCodeQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class ConnectionServiceComponent implements ConnectionService
{
	private ComponentContext context;

	private PersistenceProvider persistenceProvider;

	private EntityManager entityManager;

	private final Map<Class<? extends AbstractEntity>, AbstractEntityQuery<? extends AbstractEntity>> queries = new HashMap<Class<? extends AbstractEntity>, AbstractEntityQuery<? extends AbstractEntity>>();

	protected void activate(final ComponentContext componentContext)
	{
		this.context = componentContext;
		this.connect();
	}

	@Override
	public void connect()
	{
		log(LogService.LOG_INFO, "Starting entity manager");
		IStatus status = this.startEntityManager();
		if (status.getSeverity() == IStatus.ERROR)
		{
			String error = status.getException() == null ? "" : ": " + status.getException().getMessage();
			log(LogService.LOG_ERROR, "Error starting entity manager" + error);
			final IStatus s = status;
			UIJob job = new UIJob("Fehlermeldung")
			{
				@Override
				public IStatus runInUIThread(final IProgressMonitor monitor)
				{
					Shell shell = new Shell(this.getDisplay());
					StringBuilder msg = new StringBuilder(
							"Die Verbindung zur Datenbank konnte nicht hergestellt werden.");
					msg = msg.append(" Das Programm wird verlassen.");

					ErrorDialog dialog = new ErrorDialog(shell, "Datenbankfehler", msg.toString(), s, 0);
					dialog.open();
					return Status.OK_STATUS;
				}
			};
			job.addJobChangeListener(new JobChangeAdapter()
			{
				@Override
				public void done(final IJobChangeEvent event)
				{
					if (PlatformUI.isWorkbenchRunning())
						PlatformUI.getWorkbench().close();
					else
						System.exit(-1);
				}
			});
			job.schedule();
		}
		else
		{
			// if (result.equals(ResultType.UPDATE_CURRENT_VERSION))
			// {
			// status = new DatabaseConfigurer().configureDatabase();
			// }
			// if (status.getSeverity() == IStatus.OK)
			// {
			// status = new PredefinedEntityChecker().check();
			// }
		}
		if (status.getSeverity() == IStatus.ERROR)
		{
			if (PlatformUI.isWorkbenchRunning())
				PlatformUI.getWorkbench().close();
			else
				System.exit(-1);
		}
	}

	protected void deactivate(final ComponentContext componentContext)
	{
		this.disconnect();
		this.context = null;
	}

	private void disconnect()
	{
		if (entityManager != null)
		{
			// entityManager.flush();
			entityManager.close();
		}
	}

	@Override
	public ComponentContext getContext()
	{
		return context;
	}

	@Override
	public EntityManager getEntityManager()
	{
		return entityManager;
	}

	@Override
	public AbstractEntityQuery<? extends AbstractEntity> getQuery(final Class<? extends AbstractEntity> clazz)
	{
		AbstractEntityQuery<? extends AbstractEntity> query = queries.get(clazz);
		if (query == null)
		{
			if (clazz.equals(Address.class))
			{
				query = new AddressQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(AddressGroup.class))
			{
				query = new AddressGroupQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(AddressGroupCategory.class))
			{
				query = new AddressGroupCategoryQuery(this);
				queries.put(clazz, query);
			}
			// else if (clazz.equals(AddressGroupLink.class))
			// {
			// query = new AddressGroupLinkQuery(this);
			// queries.put(clazz, query);
			// }
			else if (clazz.equals(AddressGroupMember.class))
			{
				query = new AddressGroupMemberQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(AddressSalutation.class))
			{
				query = new AddressSalutationQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(AddressType.class))
			{
				query = new AddressTypeQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Appliance.class))
			{
				query = new ApplianceQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Booking.class))
			{
				query = new BookingQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(BookingType.class))
			{
				query = new BookingTypeQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Category.class))
			{
				query = new CategoryQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Compensation.class))
			{
				query = new CompensationQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(CompensationType.class))
			{
				query = new CompensationTypeQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(FieldExtension.class))
			{
				query = new FieldExtensionQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Country.class))
			{
				query = new CountryQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Course.class))
			{
				query = new CourseQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(CourseDetail.class))
			{
				query = new CourseDetailQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(CourseGuide.class))
			{
				query = new CourseGuideQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Domain.class))
			{
				query = new DomainQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Donation.class))
			{
				query = new DonationQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(DonationPurpose.class))
			{
				query = new DonationPurposeQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(EmailAccount.class))
			{
				query = new EmailAccountQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(GlobalSettings.class))
			{
				query = new GlobalSettingsQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Guide.class))
			{
				query = new GuideQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(GuideType.class))
			{
				query = new GuideTypeQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(LinkPersonAddress.class))
			{
				query = new LinkPersonAddressQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Member.class))
			{
				query = new MemberQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Membership.class))
			{
				query = new MembershipQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Participant.class))
			{
				query = new ParticipantQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Person.class))
			{
				query = new PersonQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(PersonSettings.class))
			{
				query = new PersonSettingsQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(PersonSex.class))
			{
				query = new PersonSexQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(PersonTitle.class))
			{
				query = new PersonTitleQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Rubric.class))
			{
				query = new RubricQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(SchoolClass.class))
			{
				query = new SchoolClassQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Season.class))
			{
				query = new SeasonQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Teacher.class))
			{
				query = new TeacherQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(User.class))
			{
				query = new UserQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Version.class))
			{
				query = new VersionQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Visit.class))
			{
				query = new VisitQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(VisitAppliance.class))
			{
				query = new VisitApplianceQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(Visitor.class))
			{
				query = new VisitorQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(VisitSettings.class))
			{
				query = new VisitSettingsQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(VisitTheme.class))
			{
				query = new VisitThemeQuery(this);
				queries.put(clazz, query);
			}
			else if (clazz.equals(ZipCode.class))
			{
				query = new ZipCodeQuery(this);
				queries.put(clazz, query);
			}
		}
		return query;
	}

	@Override
	public Session getSession()
	{
		if (getEntityManager() != null)
		{
			JpaEntityManager jpaEntityManager = (JpaEntityManager) getEntityManager().getDelegate();
			return jpaEntityManager.getActiveSession();
		}
		return null;
	}

	private void log(final int level, final String message)
	{
		Activator.log(level, message);
	}

	protected void setPersistenceProvider(final PersistenceProvider persistenceProvider)
	{
		this.persistenceProvider = persistenceProvider;
	}

	private IStatus startEntityManager()
	{
		IStatus status = Status.OK_STATUS;
		try
		{
			Map<String, Object> properties = Activator.getDefault().getProperties();
			EntityManagerFactory emf = persistenceProvider.createEntityManagerFactory("ch.eugster.events.persistence",
					properties);
			if (emf != null)
			{
				entityManager = emf.createEntityManager();
				DatabaseConfigurer configurer = new DatabaseConfigurer();
				configurer.configureDatabase();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Fehler beim Starten des EntityManagers", e);
		}
		return status;
	}

	protected void unsetPersistenceProvider(final PersistenceProvider persistenceProvider)
	{
		this.persistenceProvider = null;
	}
}
