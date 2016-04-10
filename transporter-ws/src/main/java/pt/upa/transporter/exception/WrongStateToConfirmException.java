package pt.upa.transporter.exception;

import pt.upa.transporter.ws.JobStateView;


public class WrongStateToConfirmException extends TransporterException{
    private static final long serialVersionUID = 1L;

    public WrongStateToConfirmException() {}

    @Override
    public String getMessage() {
        return "The Job is not in the valid state to confirm ";
    }
}
