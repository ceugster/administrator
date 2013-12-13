package ch.eugster.events.course.wizards;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.formattedtext.MaskFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.eugster.events.course.Activator;
import ch.eugster.events.course.Constants;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingForthcomingState;
import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Membership;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.person.views.ICriteriaChangedListener;
import ch.eugster.events.person.views.LinkSearcher;
import ch.eugster.events.person.views.PersonSorter;
import ch.eugster.events.ui.dnd.EntityTransfer;
import ch.eugster.events.ui.dnd.LinkPersonAddressDragSourceListener;

public class ParticipantWizardPage extends WizardPage implements ISelectionChangedListener, IBookingWizardPage
{
	/**
	 * The ViewerRoot caches the changes to the items of the booking. Clones of
	 * the bookings's existing participants are added to this instance. Changes
	 * to participants and adding new participants and removing participants are
	 * reflected by this instance. In the update method, the changes of existing
	 * participants are copied to the originals residing in booking and new
	 * participants added.
	 */
	// private ViewerRoot root;

	private final Map<Long, BookingType> bookingTypes = new HashMap<Long, BookingType>();

	private LinkSearcher searcher;

	private TableViewer selectionViewer;

	private TableViewer participantViewer;

	private Label found;

	private Label selected;

	private IDialogSettings dialogSettings;

	private final Collection<ISelectionChangedListener> selectionChangedListeners = new ArrayList<ISelectionChangedListener>();

	public ParticipantWizardPage(final String pageName, final IBookingWizard wizard)
	{
		super(pageName);
		Assert.isTrue(wizard instanceof BookingWizard);
		this.dialogSettings = Activator.getDefault().getDialogSettings()
				.getSection(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_ID);
		if (this.dialogSettings == null)
		{
			this.dialogSettings = Activator.getDefault().getDialogSettings()
					.addNewSection(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_ID);
		}
		if (this.dialogSettings.getArray(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_OUTER_SASH_WEIGHTS) == null)
			this.dialogSettings.put(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_OUTER_SASH_WEIGHTS,
					new String[] { "554", "329" });
		if (this.dialogSettings.getArray(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_INNER_SASH_WEIGHTS) == null)
			this.dialogSettings.put(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_INNER_SASH_WEIGHTS,
					new String[] { "554", "329" });
		if (this.dialogSettings.getArray(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_SEARCHER_SIZE) == null)
			this.dialogSettings.put(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_SEARCHER_SIZE, new String[] {
					"328", "329" });
		if (this.dialogSettings.getArray(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_LINK_SIZE) == null)
			this.dialogSettings.put(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_LINK_SIZE, new String[] {
					"554", "280" });
		if (this.dialogSettings.getArray(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_PARTICIPANT_SIZE) == null)
			this.dialogSettings.put(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_PARTICIPANT_SIZE,
					new String[] { "883", "200" });

	}

	@SuppressWarnings("unchecked")
	private void addParticipants(final StructuredSelection selectedLinks)
	{
		List<LinkPersonAddress> links = selectedLinks.toList();
		for (LinkPersonAddress link : links)
		{
			if (canAdd(1))
			{
				BookingWizard wizard = (BookingWizard) this.getWizard();
				Participant participant = Participant.newInstance(link, wizard.getBooking());
				if (wizard.getBooking().getParticipant() == null)
				{
					wizard.getBooking().setParticipant(participant);
				}
				wizard.getBooking().addParticipant(participant);
				this.participantViewer.refresh();
			}
			else
			{
				MessageDialog.openWarning(this.getShell(), "Teilnehmerzahl überschritten",
						"Es können keine weiteren Teilnehmer erfasst werden, da die maximale Anzahl erreicht ist.");
			}
		}
	}

	public void addSelectionChangedListener(final ISelectionChangedListener listener)
	{
		this.selectionChangedListeners.add(listener);
	}

	private boolean canAdd(final int count)
	{
		boolean canAdd = true;
		Booking booking = ((BookingWizard) this.getWizard()).getBooking();
		if (booking.getForthcomingState().equals(BookingForthcomingState.BOOKED))
		{
			int max = booking.getCourse().getMaxParticipants();
			int courseBooked = booking.getCourse().getBookedParticipantsCount();
			canAdd = count <= max - courseBooked;
		}
		return canAdd;
	}

	private boolean canAdd(final Participant participant, final int count)
	{
		boolean canAdd = true;
		Booking booking = ((BookingWizard) this.getWizard()).getBooking();
		if (booking.getForthcomingState().equals(BookingForthcomingState.BOOKED)
				|| booking.getForthcomingState().equals(BookingForthcomingState.PROVISIONAL_BOOKED))
		{
			int max = booking.getCourse().getMaxParticipants();
			int courseBooked = booking.getCourse().getBookedParticipantsCount(true);
			canAdd = count <= max - courseBooked;
		}
		return canAdd;
	}

	@Override
	public void createControl(final Composite parent)
	{
		this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("BOOKING_48"));
		this.setTitle("Auswahl Teilnehmer");
		this.setMessage("Bearbeiten der Teilnehmerauswahl");

		final SashForm mainSash = new SashForm(parent, SWT.VERTICAL);
		mainSash.setLayoutData(new GridData(GridData.FILL_BOTH));
		mainSash.setLayout(new GridLayout(2, false));

		final SashForm topSash = new SashForm(mainSash, SWT.HORIZONTAL);
		topSash.setLayoutData(new GridData(GridData.FILL_BOTH));
		topSash.setLayout(new GridLayout(2, false));

		String[] values = this.dialogSettings
				.getArray(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_SEARCHER_SIZE);
		int xs = Integer.valueOf(values[0]).intValue();
		int ys = Integer.valueOf(values[1]).intValue();

		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.widthHint = xs;
		layoutData.heightHint = ys;

		Composite composite = new Composite(topSash, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(layoutData);

		this.searcher = new LinkSearcher(composite, false, SWT.NONE);
		this.searcher.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.searcher.addControlListener(new ControlAdapter()
		{
			@Override
			public void controlResized(final ControlEvent e)
			{
				Point size = ParticipantWizardPage.this.searcher.getSize();
				String[] s = new String[] { Integer.valueOf(size.x).toString(), Integer.valueOf(size.y).toString() };
				ParticipantWizardPage.this.dialogSettings.put(
						Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_SEARCHER_SIZE, s);

				int[] weights = topSash.getWeights();
				s = new String[] { Integer.valueOf(weights[0]).toString(), Integer.valueOf(weights[1]).toString() };
				ParticipantWizardPage.this.dialogSettings.put(
						Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_INNER_SASH_WEIGHTS, s);
			}
		});

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttonComposite.setLayout(new GridLayout(2, false));

		Label label = new Label(buttonComposite, SWT.None);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button clear = new Button(buttonComposite, SWT.PUSH);
		clear.setLayoutData(new GridData());
		clear.setImage(Activator.getDefault().getImageRegistry().get("CLEAR"));
		clear.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				ParticipantWizardPage.this.searcher.clearSearchFields();
			}

		});

		composite = new Composite(topSash, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(layout);
		composite.addControlListener(new ControlAdapter()
		{
			@Override
			public void controlResized(final ControlEvent e)
			{
				Point size = ParticipantWizardPage.this.selectionViewer.getTable().getSize();
				String[] s = new String[] { Integer.valueOf(size.x).toString(), Integer.valueOf(size.y).toString() };
				ParticipantWizardPage.this.dialogSettings.put(
						Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_LINK_SIZE, s);

				int[] weights = topSash.getWeights();
				s = new String[] { Integer.valueOf(weights[0]).toString(), Integer.valueOf(weights[1]).toString() };
				ParticipantWizardPage.this.dialogSettings.put(
						Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_INNER_SASH_WEIGHTS, s);
			}
		});

		values = this.dialogSettings.getArray(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_INNER_SASH_WEIGHTS);
		int lw = Integer.valueOf(values[0]).intValue();
		int rw = Integer.valueOf(values[1]).intValue();

		topSash.setWeights(new int[] { lw, rw });

		values = this.dialogSettings.getArray(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_LINK_SIZE);
		int xl = Integer.valueOf(values[0]).intValue();
		int yl = Integer.valueOf(values[1]).intValue();

		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.widthHint = xl;
		layoutData.heightHint = yl;

		Table table = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setLayoutData(layoutData);
		table.setHeaderVisible(true);
		/**
		 * This viewer shows the LinkPersonAddress selection by the searcher.
		 * The entries of this viewer can be selected by the user and dropped
		 * into the participant viewer where all the participants of this
		 * booking are shown.
		 * 
		 */
		this.selectionViewer = new TableViewer(table);
		this.selectionViewer.setContentProvider(new PersonContentProvider());
		this.selectionViewer.setSorter(new PersonSorter());
		this.selectionViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.selectionViewer.addDoubleClickListener(new IDoubleClickListener()
		{
			@Override
			public void doubleClick(final DoubleClickEvent event)
			{
				StructuredSelection ssel = (StructuredSelection) ParticipantWizardPage.this.selectionViewer
						.getSelection();
				if (canAdd(ssel.size()))
				{
					ParticipantWizardPage.this.addParticipants(ssel);
				}
			}
		});
		this.selectionViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				int size = ((StructuredSelection) event.getSelection()).size();
				ParticipantWizardPage.this.selected.setText("Ausgewählt: " + size);
			}
		});

		this.searcher.addCriteriaChangedListener(new ICriteriaChangedListener()
		{
			@Override
			public void criteriaChanged(final AbstractEntity[] entities)
			{
				Collection<LinkPersonAddress> revisedLinks = new ArrayList<LinkPersonAddress>();
				// BookingWizard wizard = (BookingWizard) this.getWizard();
				// ViewerRoot root = (ViewerRoot)
				// ParticipantWizardPage.this.participantViewer.getInput();
				for (AbstractEntity entity : entities)
				{
					if (entity instanceof Person)
					{
						Person person = (Person) entity;
						Collection<LinkPersonAddress> links = person.getLinks();
						for (LinkPersonAddress link : links)
						{
							revisedLinks.add(link);
						}
					}
				}

				ParticipantWizardPage.this.selectionViewer.setInput(revisedLinks.toArray(new LinkPersonAddress[0]));
				ParticipantWizardPage.this.found.setText("Gefunden: " + revisedLinks.size());
				int size = ((StructuredSelection) ParticipantWizardPage.this.selectionViewer.getSelection()).size();
				ParticipantWizardPage.this.selected.setText("Ausgewählt: " + size);
				ParticipantWizardPage.this.packColumns(ParticipantWizardPage.this.selectionViewer);
			}
		});

		Transfer[] transfers = new Transfer[] { EntityTransfer.getTransfer() };
		int ops = DND.DROP_COPY;
		this.selectionViewer.addDragSupport(ops, transfers, new LinkPersonAddressDragSourceListener(
				this.selectionViewer));

		TableViewerColumn tableViewerColumn = new TableViewerColumn(this.selectionViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					Person person = link.getPerson();
					cell.setText(PersonFormatter.getInstance().formatId(person) + (person.isMember() ? "*" : ""));
					cell.setImage(Activator.getDefault().getImageRegistry().get("PERSON_BLUE"));
				}
			}
		});
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText("Code");
		tableColumn.setResizable(true);
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				((PersonSorter) ParticipantWizardPage.this.selectionViewer.getSorter()).setCurrentColumn(0);
				ParticipantWizardPage.this.selectionViewer.refresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.selectionViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					Person person = link.getPerson();
					cell.setText(PersonFormatter.getInstance().formatLastnameFirstname(person));
					cell.setImage(null);
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText("Name");
		tableColumn.setResizable(true);
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				((PersonSorter) ParticipantWizardPage.this.selectionViewer.getSorter()).setCurrentColumn(1);
				ParticipantWizardPage.this.selectionViewer.refresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.selectionViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					cell.setText(AddressFormatter.getInstance().formatAddressLine(link.getAddress()));
					cell.setImage(null);
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText("Anschrift");
		tableColumn.setResizable(true);
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				((PersonSorter) ParticipantWizardPage.this.selectionViewer.getSorter()).setCurrentColumn(3);
				ParticipantWizardPage.this.selectionViewer.refresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.selectionViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					cell.setText(AddressFormatter.getInstance().formatCityLine(link.getAddress()));
					cell.setImage(null);
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText("PLZ Ort");
		tableColumn.setResizable(true);
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				((PersonSorter) ParticipantWizardPage.this.selectionViewer.getSorter()).setCurrentColumn(4);
				ParticipantWizardPage.this.selectionViewer.refresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.selectionViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					Person person = link.getPerson();
					cell.setText(PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(person.getCountry(),
							person.getPhone()));
					cell.setImage(null);
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText("Handy");
		tableColumn.setResizable(true);
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				((PersonSorter) ParticipantWizardPage.this.selectionViewer.getSorter()).setCurrentColumn(5);
				ParticipantWizardPage.this.selectionViewer.refresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.selectionViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					Address address = link.getAddress();
					if (address.getCountry() != null && !address.getCountry().getPhonePattern().isEmpty())
					{
						MaskFormatter formatter = new MaskFormatter(address.getCountry().getPhonePattern());
						formatter.setValue(address.getPhone());
						cell.setText(formatter.getDisplayString());
					}
					else
					{
						cell.setText(address.getPhone());
					}
					cell.setImage(null);
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText("Telefon");
		tableColumn.setResizable(true);
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				((PersonSorter) ParticipantWizardPage.this.selectionViewer.getSorter()).setCurrentColumn(6);
				ParticipantWizardPage.this.selectionViewer.refresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.selectionViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					Address address = link.getAddress();
					if (address.getCountry() != null && !address.getCountry().getPhonePattern().isEmpty())
					{
						MaskFormatter formatter = new MaskFormatter(address.getCountry().getPhonePattern());
						formatter.setValue(address.getFax());
						cell.setText(formatter.getDisplayString());
					}
					else
					{
						cell.setText(address.getFax());
					}
					cell.setImage(null);
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText("Fax");
		tableColumn.setResizable(true);
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				((PersonSorter) ParticipantWizardPage.this.selectionViewer.getSorter()).setCurrentColumn(7);
				ParticipantWizardPage.this.selectionViewer.refresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.selectionViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					Person person = link.getPerson();
					cell.setText(person.getEmail());
					cell.setImage(null);
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText("Email");
		tableColumn.setResizable(true);
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				((PersonSorter) ParticipantWizardPage.this.selectionViewer.getSorter()).setCurrentColumn(8);
				ParticipantWizardPage.this.selectionViewer.refresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.selectionViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					Person person = link.getPerson();
					cell.setText(person.getDomain() == null ? "" : person.getDomain().getCode());
					cell.setImage(null);
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText("Domäne");
		tableColumn.setResizable(true);
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				((PersonSorter) ParticipantWizardPage.this.selectionViewer.getSorter()).setCurrentColumn(9);
				ParticipantWizardPage.this.selectionViewer.refresh();
			}
		});

		Composite info = new Composite(composite, SWT.NONE);
		info.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		info.setLayout(new GridLayout(2, true));

		this.found = new Label(info, SWT.NONE);
		this.found.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.selected = new Label(info, SWT.NONE);
		this.selected.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.selectionViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				ParticipantWizardPage.this.selected.setText("Ausgewählt: "
						+ ((StructuredSelection) event.getSelection()).size());
			}
		});

		layoutData = new GridData(GridData.FILL_BOTH);

		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		final Composite bottomComposite = new Composite(mainSash, SWT.NONE);
		bottomComposite.setLayoutData(layoutData);
		bottomComposite.setLayout(layout);
		bottomComposite.addControlListener(new ControlAdapter()
		{
			@Override
			public void controlResized(final ControlEvent e)
			{
				Point size = ParticipantWizardPage.this.participantViewer.getTable().getSize();
				String[] s = new String[] { Integer.valueOf(size.x).toString(), Integer.valueOf(size.y).toString() };
				ParticipantWizardPage.this.dialogSettings.put(
						Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_PARTICIPANT_SIZE, s);

				int[] weights = mainSash.getWeights();
				s = new String[] { Integer.valueOf(weights[0]).toString(), Integer.valueOf(weights[1]).toString() };
				ParticipantWizardPage.this.dialogSettings.put(
						Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_OUTER_SASH_WEIGHTS, s);
			}
		});

		values = this.dialogSettings.getArray(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_OUTER_SASH_WEIGHTS);
		int st = Integer.valueOf(values[0]).intValue();
		int sb = Integer.valueOf(values[1]).intValue();

		mainSash.setWeights(new int[] { st, sb });

		values = this.dialogSettings.getArray(Constants.DIALOG_SETTINGS_KEY_PARTICIPANT_WIZARD_PAGE_PARTICIPANT_SIZE);
		int xp = Integer.valueOf(values[0]).intValue();
		int yp = Integer.valueOf(values[1]).intValue();

		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.widthHint = xp;
		layoutData.heightHint = yp;

		table = new Table(bottomComposite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setLayoutData(layoutData);
		table.setHeaderVisible(true);
		/**
		 * This viewer shows up all valid participants of the current booking.
		 * They are selected by the user while starting the wizard or by
		 * dropping from the selection viewer (see before).
		 * 
		 * Changes on this viewer are monitored as SelectionChangedEvents
		 */
		this.participantViewer = new TableViewer(table);
		this.participantViewer.setContentProvider(new ParticipantContentProvider());
		this.participantViewer.setSorter(new PersonSorter());
		this.participantViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });

		final MenuManager menuManager = new MenuManager("#participantWizardPagePopupMenu"); //$NON-NLS-1$
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener()
		{
			@Override
			public void menuAboutToShow(final IMenuManager manager)
			{
				StructuredSelection ssel = (StructuredSelection) ParticipantWizardPage.this.participantViewer
						.getSelection();
				int size = ssel.size();

				if (size > 0)
				{
					if (ssel.size() == 1)
					{
						ParticipantWizardPage.this.createSetCorrespondentAction(manager);
						manager.add(new Separator());
					}
					ParticipantWizardPage.this.createDeleteParticipantAction(manager);
				}
			}
		});
		Menu menu = menuManager.createContextMenu(this.participantViewer.getTable());
		this.participantViewer.getTable().setMenu(menu);

		BookingWizard wizard = (BookingWizard) this.getWizard();
		if (wizard.getBooking().getCourse() != null)
		{
			this.loadBookingTypes(wizard.getBooking().getCourse());
		}
		// this.root = new ViewerRoot(this.participantViewer);
		// this.root.addSelectionChangedListener(this);
		// this.root.setBooking(wizard.getBooking());

		tableViewerColumn = new TableViewerColumn(this.participantViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Participant)
				{
					Participant participant = (Participant) object;
					Person person = participant.getLink().getPerson();
					cell.setText(PersonFormatter.getInstance().formatId(person));

					BookingWizard wizard = (BookingWizard) getWizard();
					Booking booking = wizard.getBooking();
					if (booking.getParticipant() != null)
					{
						if (participant == booking.getParticipant())
						{
							cell.setImage(Activator.getDefault().getImageRegistry().get("BOOKING_RED"));
							return;
						}
					}
					cell.setImage(Activator.getDefault().getImageRegistry().get("BOOKING_BLUE"));
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText("Code");
		tableColumn.setResizable(true);
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				((PersonSorter) ParticipantWizardPage.this.participantViewer.getSorter()).setCurrentColumn(0);
				ParticipantWizardPage.this.participantViewer.refresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.participantViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Participant)
				{
					Participant participant = (Participant) object;
					Person person = participant.getLink().getPerson();
					cell.setText(PersonFormatter.getInstance().formatLastnameFirstname(person));
					cell.setImage(null);
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText("Name");
		tableColumn.setResizable(true);
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				((PersonSorter) ParticipantWizardPage.this.participantViewer.getSorter()).setCurrentColumn(1);
				ParticipantWizardPage.this.participantViewer.refresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.participantViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Participant)
				{
					Participant participant = (Participant) object;
					LinkPersonAddress link = participant.getLink();
					cell.setText(AddressFormatter.getInstance().formatAddressLine(link.getAddress()));
					cell.setImage(null);
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText("Anschrift");
		tableColumn.setResizable(true);
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				((PersonSorter) ParticipantWizardPage.this.participantViewer.getSorter()).setCurrentColumn(3);
				ParticipantWizardPage.this.participantViewer.refresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.participantViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Participant)
				{
					Participant participant = (Participant) object;
					LinkPersonAddress link = participant.getLink();
					cell.setText(AddressFormatter.getInstance().formatCityLine(link.getAddress()));
					cell.setImage(null);
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText("Ort");
		tableColumn.setResizable(true);
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				((PersonSorter) ParticipantWizardPage.this.participantViewer.getSorter()).setCurrentColumn(3);
				ParticipantWizardPage.this.participantViewer.refresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.participantViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object element = cell.getElement();
				if (element instanceof Participant)
				{
					Participant participant = (Participant) element;
					if (participant.getBookingType() != null
							&& !ParticipantWizardPage.this.bookingTypes.containsKey(participant.getBookingType()
									.getId()))
					{
						participant.setBookingType(null);
					}
					if (participant.getBookingType() != null)
						cell.setText(participant.getBookingType().getComboLabel());
				}
			}
		});
		tableViewerColumn.setEditingSupport(new EditingSupport(this.participantViewer)
		{

			@Override
			protected boolean canEdit(final Object element)
			{
				return true;
			}

			@Override
			protected CellEditor getCellEditor(final Object element)
			{
				return new ComboBoxCellEditor(ParticipantWizardPage.this.participantViewer.getTable(),
						ParticipantWizardPage.this.getBookingTypeLabels());
			}

			@Override
			protected Object getValue(final Object element)
			{
				Participant participant = (Participant) element;
				if (participant.getBookingType() == null)
				{
					return Integer.valueOf(0);
				}
				else
				{
					BookingType[] bookingTypes = ParticipantWizardPage.this.bookingTypes.values().toArray(
							new BookingType[0]);
					for (int i = 0; i < bookingTypes.length; i++)
					{
						if (bookingTypes[i].getId().equals(participant.getBookingType().getId()))
							return Integer.valueOf(i);
					}
					return Integer.valueOf(0);
				}
			}

			@Override
			protected void setValue(final Object element, final Object value)
			{
				BookingType[] bookingTypes = ParticipantWizardPage.this.bookingTypes.values().toArray(
						new BookingType[0]);
				if (bookingTypes.length > 0)
				{
					int index = ((Integer) value).intValue();
					if (index >= 0 && index < bookingTypes.length)
					{
						Participant participant = (Participant) element;
						participant.setBookingType(bookingTypes[((Integer) value).intValue()]);
						ParticipantWizardPage.this.participantViewer.refresh(element);
					}
				}
				updatePageState();
			}

		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText("Buchungsart");
		tableColumn.setResizable(true);
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				((PersonSorter) ParticipantWizardPage.this.participantViewer.getSorter()).setCurrentColumn(9);
				ParticipantWizardPage.this.participantViewer.refresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.participantViewer, SWT.RIGHT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object element = cell.getElement();
				if (element instanceof Participant)
				{
					Participant participant = (Participant) element;
					cell.setText(DecimalFormat.getIntegerInstance().format(participant.getCount()));
				}
			}
		});
		tableViewerColumn.setEditingSupport(new EditingSupport(this.participantViewer)
		{
			@Override
			protected boolean canEdit(final Object element)
			{
				return true;
			}

			@Override
			protected CellEditor getCellEditor(final Object element)
			{
				final Participant participant = (Participant) element;
				CellEditor editor = new TextCellEditor(ParticipantWizardPage.this.participantViewer.getTable(),
						SWT.RIGHT);
				editor.setValidator(new ICellEditorValidator()
				{
					@Override
					public String isValid(final Object value)
					{
						String val = value.toString();
						val = val.isEmpty() ? "0" : val;
						try
						{
							Integer count = Integer.valueOf(val);
							if (!canAdd(participant, count.intValue()))
							{
								return "Die Teilnehmerzahl ist überschritten.";
							}
						}
						catch (NumberFormatException e)
						{
							return "Ungültige Eingabe";
						}
						return null;
					}
				});
				return editor;
			}

			@Override
			protected Object getValue(final Object element)
			{
				Participant participant = (Participant) element;
				return DecimalFormat.getIntegerInstance().format(participant.getCount());
			}

			@Override
			protected void setValue(final Object element, final Object value)
			{
				if (value != null)
				{
					try
					{
						int val = Integer.valueOf(value.toString()).intValue();
						Participant participant = (Participant) element;
						participant.setCount(val);
						participantViewer.update(participant, null);
						setPageComplete(true);
					}
					catch (NumberFormatException e)
					{

					}
				}
			}

		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText("Teilnehmer");
		tableColumn.setResizable(true);

		transfers = new Transfer[] { EntityTransfer.getTransfer() };
		ops = DND.DROP_COPY;
		this.participantViewer.addDropSupport(ops, transfers, new ParticipantViewerDropAdapter(this.participantViewer,
				wizard.getBooking()));

		this.participantViewer.setInput(wizard.getBooking());

		ParticipantWizardPage.this.packColumns(this.participantViewer);

		this.searcher.initialize();

		this.setControl(mainSash);

		this.updatePageState();
	}

	private void createDeleteParticipantAction(final IMenuManager manager)
	{
		Action action = new Action()
		{
			@SuppressWarnings("rawtypes")
			@Override
			public void run()
			{
				super.run();
				BookingWizard wizard = (BookingWizard) getWizard();
				StructuredSelection ssel = (StructuredSelection) ParticipantWizardPage.this.participantViewer
						.getSelection();
				Iterator iter = ssel.iterator();
				while (iter.hasNext())
				{
					Participant participant = (Participant) iter.next();
					wizard.getBooking().removeParticipant(participant);
					// ViewerRoot parent = (ViewerRoot)
					// ParticipantWizardPage.this.participantViewer.getInput();
					// parent.removeParticipant(participant);
				}
				participantViewer.refresh();
			}
		};
		action.setText("Teilnehmer entfernen");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("DELETE"));
		manager.add(action);
	}

	private void createSetCorrespondentAction(final IMenuManager manager)
	{
		Action action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				BookingWizard wizard = (BookingWizard) getWizard();
				StructuredSelection ssel = (StructuredSelection) ParticipantWizardPage.this.participantViewer
						.getSelection();
				if (ssel.size() == 1)
				{
					Participant participant = (Participant) ssel.getFirstElement();
					// ((ViewerRoot)
					// ParticipantWizardPage.this.participantViewer.getInput())
					// .setDefaultParticipant(participant);
					wizard.getBooking().setParticipant(participant);
				}
			}
		};
		action.setText("Hauptteilnehmer setzen");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("BOOKING_RED"));
		action.setEnabled(this.getCorrespondentActionState());
		manager.add(action);
	}

	@Override
	public void dispose()
	{
		this.searcher.dispose();
	}

	private String[] getBookingTypeLabels()
	{
		String[] labels = new String[0];
		if (this.bookingTypes == null)
		{
			labels = new String[0];
		}
		else
		{
			BookingType[] bookingTypes = this.bookingTypes.values().toArray(new BookingType[0]);
			labels = new String[bookingTypes.length];
			for (int i = 0; i < bookingTypes.length; i++)
			{
				labels[i] = bookingTypes[i].getComboLabel();
			}
		}
		return labels;
	}

	private boolean getCorrespondentActionState()
	{
		if (this.participantViewer.getSelection().isEmpty())
			return false;

		BookingWizard wizard = (BookingWizard) getWizard();
		// Participant currentDefaultParticipant = ((ViewerRoot)
		// ParticipantWizardPage.this.participantViewer.getInput())
		// .getDefaultParticipant();
		if (wizard.getBooking().getParticipant() == null)
			return true;

		Participant selectedParticipant = (Participant) ((StructuredSelection) this.participantViewer.getSelection())
				.getFirstElement();
		return !selectedParticipant.equals(wizard.getBooking().getParticipant());
	}

	private void loadBookingTypes(final Course course)
	{
		this.bookingTypes.clear();
		for (BookingType bookingType : course.getBookingTypes())
		{
			if (!bookingType.isDeleted())
			{
				this.bookingTypes.put(bookingType.getId(), bookingType);
			}
		}
		this.participantViewer.refresh(true);

	}

	private void packColumns(final TableViewer viewer)
	{
		TableColumn[] columns = viewer.getTable().getColumns();
		for (TableColumn column : columns)
			column.pack();
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event)
	{
		if (event.getSource().equals(this.participantViewer))
		{
			this.participantViewer.refresh();
			this.packColumns(this.participantViewer);

			BookingWizard wizard = (BookingWizard) getWizard();
			this.setPageComplete(wizard.getBooking().getParticipant() != null
					&& wizard.getBooking().getParticipantCount() > 0);

			ISelectionChangedListener[] listeners = this.selectionChangedListeners
					.toArray(new ISelectionChangedListener[0]);
			for (ISelectionChangedListener listener : listeners)
				listener.selectionChanged(event);
		}
		else
		{
			if (event.getSelection() instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				if (!ssel.isEmpty() && ssel.getFirstElement() instanceof Course)
				{
					Course course = (Course) ssel.getFirstElement();
					this.loadBookingTypes(course);
				}
			}
		}
	}

	public void update(final Booking booking)
	{
		System.out.println(booking);
	}

	public void updatePageState()
	{
		Table table = this.participantViewer.getTable();
		TableItem[] items = table.getItems();
		for (TableItem item : items)
		{
			Participant participant = (Participant) item.getData();
			if (participant.getBookingType() == null)
			{
				super.setPageComplete(false);
				return;
			}
			if (participant.getCount() < 1)
			{
				super.setPageComplete(false);
				return;
			}
		}
		super.setPageComplete(items.length > 0);
	}

	private class ParticipantContentProvider implements IStructuredContentProvider
	{

		@Override
		public void dispose()
		{
		}

		@Override
		public Object[] getElements(final Object inputElement)
		{
			if (inputElement instanceof Booking)
			{
				Booking booking = (Booking) inputElement;
				return booking.getParticipants().toArray(new Participant[0]);
			}
			return new Participant[0];
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
		{
		}

	}

	private class ParticipantViewerDropAdapter extends ViewerDropAdapter
	{
		private final Booking booking;

		public ParticipantViewerDropAdapter(final Viewer viewer, final Booking booking)
		{
			super(viewer);
			this.booking = booking;
		}

		private BookingType checkBookingType(final BookingType bestMatch, final BookingType bookingType,
				final Participant participant)
		{
			boolean ok = true;
			if (bookingType.getMaxAge() > 0)
			{
				if (participant.getBooking().getCourse().getFirstDate() != null)
				{
					Long birthdate = participant.getLink().getPerson().getBirthdate();
					if (birthdate != null)
					{
						if (birthdate > 0 && birthdate <= GregorianCalendar.getInstance().get(Calendar.YEAR))
						{
							ok = participant.getBooking().getCourse().getFirstDate().get(Calendar.YEAR)
									- bookingType.getMaxAge() <= birthdate;
						}
						else
						{
							Calendar birth = GregorianCalendar.getInstance();
							Date dateOfBirth = birth.getTime();
							participant.getBooking().getCourse().getFirstDate()
									.add(Calendar.YEAR, -bookingType.getMaxAge());
							ok = participant.getBooking().getCourse().getFirstDate().getTime().before(dateOfBirth);
						}
					}
				}
			}
			if (ok)
			{
				Membership membership = bookingType.getMembership();
				if (membership != null)
				{
					ok = participant.getLink().getMember(membership) != null;
				}
			}
			if (ok)
			{
				if (bestMatch == null)
				{
					return bookingType;
				}
				else
				{
					return bestMatch.getPrice() < bookingType.getPrice() ? bestMatch : bookingType;
				}
			}
			else
			{
				return bestMatch;
			}
		}

		@Override
		public boolean performDrop(final Object data)
		{
			if (data instanceof LinkPersonAddress[])
			{
				if (this.getViewer().getInput() instanceof Booking)
				{
					Booking booking = (Booking) this.getViewer().getInput();
					Collection<Participant> participants = new ArrayList<Participant>();
					LinkPersonAddress[] links = (LinkPersonAddress[]) data;
					for (LinkPersonAddress link : links)
					{
						Participant participant = Participant.newInstance(link, this.booking);
						setBookingType(participant);
						participants.add(participant);
						booking.addParticipant(participant);
					}
					this.getViewer().refresh();
					ParticipantWizardPage.this.packColumns((TableViewer) this.getViewer());
					this.getViewer().setSelection(new StructuredSelection(participants.toArray(new Participant[0])));
					return true;
				}
			}
			return false;
		}

		private void setBookingType(final Participant participant)
		{
			Collection<BookingType> bookingTypes = this.booking.getCourse().getBookingTypes();
			BookingType bestMatch = null;
			for (BookingType bookingType : bookingTypes)
			{
				bestMatch = checkBookingType(bestMatch, bookingType, participant);
			}
			participant.setBookingType(bestMatch);
		}

		@Override
		public boolean validateDrop(final Object target, final int operation, final TransferData transferType)
		{
			if (EntityTransfer.getTransfer().isSupportedType(transferType))
			{
				return true;
			}
			return false;
		}
	}

	private class PersonContentProvider implements IStructuredContentProvider
	{

		@Override
		public void dispose()
		{
		}

		@Override
		public Object[] getElements(final Object inputElement)
		{
			if (inputElement instanceof LinkPersonAddress[])
				return (LinkPersonAddress[]) inputElement;

			return new LinkPersonAddress[0];
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
		{
		}

	}

	// public class ViewerRoot
	// {
	// private long tempId = 0L;
	//
	// private final TableViewer viewer;
	//
	// private Participant defaultParticipant;
	//
	// private final Collection<Participant> participants = new
	// Vector<Participant>();
	//
	// private final Collection<ISelectionChangedListener>
	// selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
	//
	// public ViewerRoot(final TableViewer viewer)
	// {
	// this.viewer = viewer;
	// }
	//
	// public void addParticipant(final Participant participant)
	// {
	// if (participant.getId() == null)
	// {
	// participant.setId(--tempId);
	// }
	// this.participants.add(participant);
	// if (participants.size() == 1)
	// {
	// this.defaultParticipant = participant;
	// }
	// this.fireSelectionChanged();
	// }
	//
	// public void addSelectionChangedListener(final ISelectionChangedListener
	// listener)
	// {
	// this.selectionChangedListeners.add(listener);
	// }
	//
	// public int countParticipants()
	// {
	// return this.getSelection().size();
	// }
	//
	// private void fireSelectionChanged()
	// {
	// SelectionChangedEvent event = new SelectionChangedEvent(this.viewer,
	// this.getSelection());
	// ISelectionChangedListener[] listeners = this.selectionChangedListeners
	// .toArray(new ISelectionChangedListener[0]);
	// for (ISelectionChangedListener listener : listeners)
	// listener.selectionChanged(event);
	// }
	//
	// public Participant getDefaultParticipant()
	// {
	// return this.defaultParticipant;
	// }
	//
	// public int getParticipantCount()
	// {
	// int count = 0;
	// Collection<Participant> participants = getParticipants();
	// for (Participant participant : participants)
	// {
	// count += participant.getCount();
	// }
	// return count;
	// }
	//
	// public Collection<Participant> getParticipants()
	// {
	// return this.participants;
	// }
	//
	// public StructuredSelection getSelection()
	// {
	// Collection<Participant> selection = new ArrayList<Participant>();
	// for (Participant participant : this.participants)
	// {
	// if (!participant.isDeleted())
	// {
	// if (!participant.getLink().isDeleted())
	// {
	// selection.add(participant);
	// }
	// }
	// }
	// return new StructuredSelection(this.participants.toArray(new
	// Participant[0]));
	// }
	//
	// public boolean isValid()
	// {
	// return !this.getSelection().isEmpty() && this.defaultParticipant != null;
	// }
	//
	// public void removeParticipant(final Participant participant)
	// {
	// if (participant.getId() == null)
	// {
	// this.participants.remove(participant);
	// }
	// else
	// {
	// participant.setDeleted(true);
	// }
	// this.fireSelectionChanged();
	// }
	//
	// public void setBooking(final Booking booking)
	// {
	// Collection<Participant> participants = booking.getParticipants();
	// for (Participant participant : participants)
	// {
	// Participant target = Participant.newInstance();
	// Participant.copy(participant, target);
	// if (target.getId() == null)
	// {
	// target.setId(new Long(--tempId));
	// }
	// if (participant == booking.getParticipant())
	// {
	// this.setDefaultParticipant(target);
	// }
	// this.participants.add(target);
	// }
	// }
	//
	// public void setDefaultParticipant(final Participant participant)
	// {
	// this.defaultParticipant = participant;
	// this.fireSelectionChanged();
	// }
	//
	// }
}
