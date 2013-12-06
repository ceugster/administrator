package ch.eugster.events.course.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.Activator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.CourseFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.queries.ParticipantQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class SelectBookingTypeDialog extends TitleAreaDialog
{
	private ComboViewer viewer;

	private Participant participant;

	public SelectBookingTypeDialog(final Shell parentShell, final Participant participant)
	{
		super(parentShell);
		this.participant = participant;
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, Dialog.OK, "Übernehmen", true);
		this.createButton(parent, Dialog.CANCEL, "Abbrechen", false);
	}

	@Override
	public Control createDialogArea(final Composite parent)
	{
		this.getShell()
				.setText(
						"Teilnehmer: "
								+ PersonFormatter.getInstance().formatLastnameFirstname(
										this.participant.getLink().getPerson()));
		this.setTitle("Buchungsart ändern");
		this.setMessage("Auswahl Buchungsarten zum Kurs "
				+ CourseFormatter.getInstance().formatComboEntry(this.participant.getBooking().getCourse()));

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(2, false));

		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Buchungsart");

		Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.viewer = new ComboViewer(combo);
		this.viewer.setContentProvider(new ArrayContentProvider());
		this.viewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public Image getImage(final Object element)
			{
				return super.getImage(element);
			}

			@Override
			public String getText(final Object element)
			{
				if (element instanceof BookingType)
				{
					return CourseFormatter.getInstance().formatBookingType(((BookingType) element));
				}
				return super.getText(element);
			}

		});
		this.viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.viewer.setInput(this.participant.getBooking().getCourse().getBookingTypes().toArray(new BookingType[0]));
		this.viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				Button button = SelectBookingTypeDialog.this.getButton(Dialog.OK);
				if (button != null && !button.isDisposed())
					button.setEnabled(!event.getSelection().isEmpty());
			}

		});
		if (this.participant.getBookingType() != null)
			this.viewer.setSelection(new StructuredSelection(this.participant.getBookingType()));

		return composite;
	}

	@Override
	protected void okPressed()
	{
		StructuredSelection ssel = (StructuredSelection) this.viewer.getSelection();
		if (ssel.isEmpty() || ((BookingType) ssel.getFirstElement()).getId() == null)
			this.participant.setBookingType(null);
		else
		{
			BookingType bookingType = (BookingType) ssel.getFirstElement();
			if (this.participant.getBookingType() == null
					|| !bookingType.getId().equals(this.participant.getBookingType().getId()))
			{
				this.participant.setBookingType((BookingType) ssel.getFirstElement());
			}
		}

		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			ParticipantQuery query = (ParticipantQuery) service.getQuery(Participant.class);
			this.participant = query.merge(this.participant);
		}
		tracker.close();
		this.close();
	}
}
