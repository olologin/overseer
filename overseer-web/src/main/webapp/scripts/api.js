var App = React.createClass({
    getInitialState: function() {
        return {
            red: 0,
            green: 0,
            blue: 0
        }
    },
    update: function (e) {
        this.setState({
            red: this.refs.red.refs.inp.getDOMNode().value,
            green: this.refs.green.refs.inp.getDOMNode().value,
            blue: this.refs.blue.refs.inp.getDOMNode().value
        });
    },
    render: function(){
        return (
            <div>
                <Slider ref="red" update={this.update} />
                <label>{this.state.red}</label>
                <Slider ref="green" update={this.update} />
                <label>{this.state.green}</label>
                <Slider ref="blue" update={this.update} />
                <label>{this.state.blue}</label>
            </div>
        );
    }
});

var Slider = React.createClass({
    render: function() {
        return (
            <div>
                <input ref="inp" type="range" min="0" max="255" onChange={this.props.update} />
            </div>
        );
    }
});


$(".dropdown-menu li a").click(function(){
    var selText = $(this).text();
    $(this).parents('.btn-group').find('.dropdown-toggle').html(selText+' <span class="caret"></span>');
});
