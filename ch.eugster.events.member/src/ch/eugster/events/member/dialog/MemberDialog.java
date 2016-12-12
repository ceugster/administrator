package ch.eugster.events.member.dialog;

import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.member.Activator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.Membership;
import ch.eugster.events.persistence.queries.AddressQuery;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.queries.MemberQuery;
import ch.eugster.events.persistence.queries.MembershipQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class MemberDialog extends TitleAreaDialog implements ICheckStateListener
{
	private final AbstractEntity entity;

	private CheckboxTableViewer membershipViewer;

	private final Hashtable<Long, Monitor> monitors = new Hashtable<Long, Monitor>();

	private final Hashtable<Long, Member> current = new Hashtable<Long, Member>();

	private final String message = "Selektieren oder Deselektieren Sie die Mitgliedschaften und speichern Sie die Änderungen.\nSie können die Änderungen jederzeit mit 'Abbrechen' widerrufen.";

	private boolean isPageComplete = false;

	/**
	 * @param parentShell
	 * @param parent
	 *            <code>parent</code> must be of type
	 *            ch.eugster.events.data.objects.Customer
	 * @param addressGroup
	 *            Falls eine neue Adressgruppe erfasst wird, muss diese bereit
	 *            vor der Übergabe an den Konstruktor von
	 *            <code>AddressGroupDialog</code> instantiiert sein und der
	 *            Parent <code>Domain</code> muss - falls ein solcher gesetzt
	 *            werden soll, ebenfalls dem Konstruktur von
	 *            <code>AddressGroup</code> übergeben worden sein.
	 * 
	 */
	public MemberDialog(final Shell parentShell, final AbstractEntity entity)
	{
		super(parentShell);
		this.setShellStyle(SWT.SHELL_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN | SWT.APPLICATION_MODAL);
		this.entity = entity;
	}

	private void checkMembership(final Membership membership)
	{
		Member member = null;
		if (entity instanceof LinkPersonAddress)
		{
			member = ((LinkPersonAddress) entity).getMember(membership);
		}
		else if (entity instanceof Address)
		{
			member = ((Address) entity).getMember(membership);
		}
		this.membershipViewer.setChecked(membership, member == null ? false : member.isDeleted() ? false : true);
	}

	@Override
	public void checkStateChanged(final CheckStateChangedEvent event)
	{
		Object object = event.getElement();
		if (object instanceof Membership)
		{
			boolean update = true;
			Membership membership = (Membership) object;
			Monitor monitor = monitors.get(membership.getId());
			String memberCode = monitor == null || monitor.code == null || monitor.code.isEmpty() ? null : monitor.code;
			if (event.getChecked())
			{
				if (memberCode == null && membership.isMemberCodeMandatory())
				{
					memberCode = showInputDialog("");
					if (memberCode == null)
					{
						membershipViewer.setChecked(membership, false);
						update = false;
					}
				}
			}
			if (update)
			{
				this.updateMonitor((Membership) object, event.getChecked(), memberCode);
			}
		}
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, "Speichern", true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);
	}

	private void createContextMenu()
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(this.membershipViewer.getControl());
		menuManager.addMenuListener(new IMenuListener()
		{
			@Override
			public void menuAboutToShow(final IMenuManager manager)
			{
				StructuredSelection ssel = (StructuredSelection) membershipViewer.getSelection();
				if (ssel.getFirstElement() instanceof Membership)
				{
					Membership membership = (Membership) ssel.getFirstElement();
					MemberDialog.this.fillContextMenu(manager, membership, membershipViewer.getChecked(membership));
				}
			}
		});
		this.membershipViewer.getControl().setMenu(menu);
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle();
		this.setMessage();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout());

		Table table = new Table(composite, SWT.BORDER | SWT.SINGLE | SWT.CHECK | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(false);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		this.membershipViewer = new CheckboxTableViewer(table);
		this.membershipViewer.setContentProvider(new MemberTableContentProvider());
		this.membershipViewer.setSorter(new MemberSorter(this));
		this.membershipViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.membershipViewer.addCheckStateListener(this);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(this.membershipViewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Membership)
				{
					Membership membership = (Membership) cell.getElement();
					cell.setText(membership.getCode());
				}
			}
		});
		tableViewerColumn.getColumn().setText("Code");

		tableViewerColumn = new TableViewerColumn(this.membershipViewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Membership)
				{
					Membership membership = (Membership) cell.getElement();
					cell.setText(membership.getName());
				}
			}
		});
		tableViewerColumn.getColumn().setText("Bezeichnung");

		tableViewerColumn = new TableViewerColumn(this.membershipViewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Membership)
				{
					Membership membership = (Membership) cell.getElement();
					Monitor monitor = monitors.get(membership.getId());
					String code = monitor == null ? null : monitor.code == null ? "" : monitor.code == null ? ""
							: monitor.code;
					cell.setText(code == null ? "" : code);
				}
			}
		});
		tableViewerColumn.getColumn().setText("Mitgliednummer");

		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				MemberQuery query = (MemberQuery) service.getQuery(Member.class);
				List<Member> members = null;
				if (this.entity instanceof LinkPersonAddress)
				{
					members = query.selectByLink((LinkPersonAddress) this.entity);
				}
				else if (this.entity instanceof Address)
				{
					members = query.selectByAddress((Address) this.entity);
				}
	
				for (Member member : members)
				{
					this.monitors.put(member.getMembership().getId(),
							new Monitor(member.getMembership(), !member.isDeleted(), member.getCode()));
					this.current.put(member.getMembership().getId(), member);
				}
	
				MembershipQuery membershipQuery = (MembershipQuery) service.getQuery(Membership.class);
				List<Membership> memberships = membershipQuery.selectAll();
				membershipViewer.setInput(memberships.toArray(new Membership[0]));
			}
		}
		finally
		{
			tracker.close();
		}
		
		Membership[] memberships = (Membership[]) membershipViewer.getInput();
		for (Membership membership : memberships)
		{
			checkMembership(membership);
		}

		TableColumn[] columns = this.membershipViewer.getTable().getColumns();
		for (TableColumn column : columns)
		{
			column.pack();
		}

		createContextMenu();

		return parent;
	}

	private void fillContextMenu(final IMenuManager menuManager, final Membership membership, final boolean checked)
	{
		Action action = new Action()
		{
			@Override
			public void run()
			{
				Member member = null;
				if (entity instanceof LinkPersonAddress)
				{
					member = ((LinkPersonAddress) entity).getMember(membership);
				}
				else if (entity instanceof Address)
				{
					member = ((Address) entity).getMember(membership);
				}
				String initialValue = (member == null || member.getCode() == null || member.getCode().isEmpty() ? ""
						: member.getCode());
				String memberCode = showInputDialog(initialValue);
				if (memberCode != null)
				{
					updateMonitor(membership, checked, memberCode);
				}
			}
		};
		action.setText("Mitgliedernummer ändern");
		menuManager.add(action);
	}

	public boolean isChecked(final Membership membership)
	{
		Monitor monitor = monitors.get(membership.getId());
		if (monitor == null)
			return false;
		else
			return monitor.checked;
	}

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	@Override
	protected void okPressed()
	{
		this.updateMembers();
		super.okPressed();
	}

	@Override
	public void setErrorMessage(final String errorMessage)
	{
		super.setErrorMessage(errorMessage);
		this.setPageComplete(false);
	}

	public void setMessage()
	{
		this.setErrorMessage(null);
		super.setMessage(this.message);
		this.setPageComplete(true);
	}

	public void setPageComplete(final boolean isComplete)
	{
		this.isPageComplete = isComplete;
		if (this.getButton(IDialogConstants.OK_ID) != null)
			this.getButton(IDialogConstants.OK_ID).setEnabled(this.isPageComplete);
	}

	public void setTitle()
	{
		super.setTitle("Mitgliedschaften");
		int width = this.getShell().getDisplay().getClientArea().width;
		int height = this.getShell().getDisplay().getClientArea().height;
		super.getShell().setMinimumSize(width > 500 ? 500 : width, height > 500 ? 500 : height);
	}

	private String showInputDialog(final String initialValue)
	{
		String title = "Mitgliedschaftsnummer";
		String message = "Erfassen Sie die Mitgliedschaftsnummer";
		IInputValidator validator = new IInputValidator()
		{
			@Override
			public String isValid(final String newText)
			{
				return newText.isEmpty() ? "" : null;
			}
		};
		InputDialog dialog = new InputDialog(this.getShell(), title, message, initialValue, validator);
		return dialog.open() == MessageDialog.OK ? dialog.getValue() : null;
	}

	public void updateMembers()
	{
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				Long[] membershipIds = this.monitors.keySet().toArray(new Long[0]);
				for (Long membershipId : membershipIds)
				{
					Member member = this.current.get(membershipId);
					Monitor monitor = this.monitors.get(membershipId);
					if (member == null)
					{
						if (monitor.checked)
						{
							if (entity instanceof LinkPersonAddress)
							{
								member = Member.newInstance(monitor.membership, (LinkPersonAddress) this.entity);
								member.getLink().addMember(member);
								member.setCode(monitor.code);
								member.setDate(GregorianCalendar.getInstance());
								// member.getMembership().addMember(member);
								LinkPersonAddressQuery query = (LinkPersonAddressQuery) service
										.getQuery(LinkPersonAddress.class);
								query.merge(member.getLink());
							}
							else if (entity instanceof Address)
							{
								member = Member.newInstance(monitor.membership, (Address) this.entity);
								member.getAddress().addMember(member);
								member.setCode(monitor.code);
								member.setDate(GregorianCalendar.getInstance());
								// member.getMembership().addMember(member);
								AddressQuery query = (AddressQuery) service.getQuery(Address.class);
								query.merge(member.getAddress());
							}
						}
					}
					else
					{
						if (monitor.checked != !member.isDeleted())
						{
							member.setCode(monitor.code);
							member.setDeleted(!monitor.checked);
							MemberQuery query = (MemberQuery) service.getQuery(Member.class);
							query.merge(member);
						}
					}
				}
			}
		}
		finally
		{
			tracker.close();
		}
	}

	private void updateMonitor(final Membership membership, final boolean checked, final String code)
	{
		Monitor monitor = this.monitors.get(membership.getId());
		if (monitor == null)
		{
			monitor = new Monitor(membership);
			this.monitors.put(membership.getId(), monitor);
		}

		monitor.checked = checked;
		monitor.code = code;

		membershipViewer.refresh(membership);
	}

	private class Monitor
	{
		public boolean checked = false;

		public String code = null;

		public Membership membership = null;

		public Monitor(final Membership membership)
		{
			this.membership = membership;
		}

		public Monitor(final Membership membership, final boolean checked, final String code)
		{
			this.membership = membership;
			this.checked = checked;
			this.code = code;
		}
	}
}
